/*
 * Copyright 2023 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper
package http
package client

import java.io.File
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong

import javax.net.SocketFactory
import javax.net.ssl.{ SSLSocketFactory, TrustManager }

import scala.util.Try
import scala.language.implicitConversions

import scamper.http.cookies.{ *, given }
import scamper.http.headers.given
import scamper.http.types.{ ContentCodingRange, MediaRange, TransferCoding }
import scamper.http.websocket.{ *, given }

import RequestMethod.Registry.*

private object HttpClientImpl:
  private val count = AtomicLong(0)

  case class Settings(
    resolveTo:           Option[Uri] = None,
    accept:              Seq[MediaRange] = Seq(MediaRange("*/*")),
    acceptEncoding:      Seq[ContentCodingRange] = Nil,
    bufferSize:          Int = 8192,
    readTimeout:         Int = 30000,
    continueTimeout:     Int = 1000,
    keepAlive:           Boolean = false,
    cookies:             CookieStore = CookieStore.Null,
    outgoing:            Seq[RequestFilter] = Nil,
    incoming:            Seq[ResponseFilter] = Nil,
    secureSocketFactory: SSLSocketFactory = SSLSocketFactory.getDefault().asInstanceOf[SSLSocketFactory]
  )

  def apply(settings: Settings): HttpClientImpl =
    new HttpClientImpl(count.incrementAndGet, settings)

private class HttpClientImpl(id: Long, settings: HttpClientImpl.Settings) extends HttpClient:
  val resolveTo       = settings.resolveTo
  val accept          = settings.accept
  val acceptEncoding  = settings.acceptEncoding
  val bufferSize      = settings.bufferSize.max(1024)
  val readTimeout     = settings.readTimeout.max(0)
  val continueTimeout = settings.continueTimeout.max(0)
  val cookies         = settings.cookies
  val keepAlive       = settings.keepAlive

  private val outgoing = settings.outgoing
  private val incoming = settings.incoming

  private val secureSocketFactory = settings.secureSocketFactory
  private val requestCount        = AtomicLong(0)

  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T =
    resolveTargetSend(request, handler)

  def get[T](target: Uri, headers: Seq[Header], cookies: Seq[PlainCookie])(handler: ResponseHandler[T]): T =
    send(HttpRequest(Get, target, headers, cookies, Entity.empty))(handler)

  def post[T](target: Uri, headers: Seq[Header], cookies: Seq[PlainCookie], body: Entity)(handler: ResponseHandler[T]): T =
    send(HttpRequest(Post, target, headers, cookies, body))(handler)

  def put[T](target: Uri, headers: Seq[Header], cookies: Seq[PlainCookie], body: Entity)(handler: ResponseHandler[T]): T =
    send(HttpRequest(Put, target, headers, cookies, body))(handler)

  def delete[T](target: Uri, headers: Seq[Header], cookies: Seq[PlainCookie])(handler: ResponseHandler[T]): T =
    send(HttpRequest(Delete, target, headers, cookies, Entity.empty))(handler)

  def websocket[T](target: Uri, headers: Seq[Header], cookies: Seq[PlainCookie])(app: WebSocketApplication[T]): T =
    val targetResolved = resolveWebSocketTarget(target)

    require(targetResolved.isAbsolute, "Absolute WebSocket URI required")
    require(targetResolved.scheme.matches("wss?"), "WebSocket scheme required")

    val req = HttpRequest(
      Get,
      targetResolved,
      Header("Upgrade", "websocket") +:
      Header("Connection", "Upgrade") +:
      Header("Sec-WebSocket-Key", WebSocket.generateKey()) +:
      Header("Sec-WebSocket-Version", "13") +:
      Header("Sec-WebSocket-Extensions", "permessage-deflate; client_no_context_takeover; server_no_context_takeover") +:
      headers,
      cookies,
      Entity.empty
    )

    sendRequest(
      req,
      res => WebSocket.checkHandshake(req, res) match
        case true =>
          val session = WebSocketSession.forClient(
            res.socket,
            res.correlate,
            res.absoluteTarget,
            req.secWebSocketVersion,
            WebSocket.enablePermessageDeflate(res)
          )
          setCloseGuard(res, true)
          try app(session)
          catch case cause: Throwable =>
            setCloseGuard(res, false)
            throw cause

        case false => throw WebSocketHandshakeFailure(s"Connection upgrade not accepted: ${res.status}")
    )

  private def resolveTarget(target: Uri): Uri =
    target.isAbsolute match
      case true  => target
      case false =>
        resolveTo.map { baseUri => target.toAbsoluteUri(baseUri.scheme, baseUri.authority) }
          .getOrElse(target)

  private def resolveWebSocketTarget(target: Uri): Uri =
    target.isAbsolute match
      case true  => target
      case false =>
        resolveTo.map { baseUri => target.toAbsoluteUri(baseUri.scheme.replace("http", "ws"), baseUri.authority) }
          .getOrElse(target)

  private def resolveTargetSend[T](request: HttpRequest, handler: ResponseHandler[T]): T =
    sendRequest(request.setTarget(resolveTarget(request.target)), handler)

  private def sendRequest[T](request: HttpRequest, handler: ResponseHandler[T]): T =
    notNull(handler)

    val target = request.target

    require(target.isAbsolute, s"Request target not absolute: $target")

    val secure         = target.scheme.matches("https|wss")
    val authority      = target.authority
    val userAgent      = request.getHeaderValueOrElse("User-Agent", "Scamper/40.0.7")
    val requestCookies = request.cookies ++ cookies.get(target)
    val connection     = getEffectiveConnection(request)

    var effectiveRequest = request.method match
      case Get     => toBodilessRequest(request)
      case Post    => toBodyRequest(request)
      case Put     => toBodyRequest(request)
      case Patch   => toBodyRequest(request)
      case Delete  => toBodilessRequest(request)
      case Head    => toBodilessRequest(request)
      case Trace   => toBodilessRequest(request)
      case Options => toBodyRequest(request)
      case _       => request

    effectiveRequest = effectiveRequest.setHeaders(
      Header("Host", authority) +:
      Header("User-Agent", userAgent) +:
      effectiveRequest.headers.filterNot(_.name.matches("(?i)Host|User-Agent|Cookie|Connection")) :+
      Header("Connection", connection)
    ).setCookies(requestCookies)

    effectiveRequest = effectiveRequest.setTarget(target.toTargetUri)

    if !effectiveRequest.path.startsWith("/") && effectiveRequest.path != "*" then
      effectiveRequest = effectiveRequest.setPath("/" + effectiveRequest.path)

    val host = target.host
    val port = target.portOption.getOrElse(if secure then 443 else 80)
    val conn = getClientConnection(secure, host, port)

    try
      val correlate = createCorrelate(requestCount.incrementAndGet)

      conn.configure(bufferSize, readTimeout, continueTimeout)

      Try(addAttributes(effectiveRequest, conn, correlate, target))
        .map(addAccept)
        .map(addAcceptEncoding)
        .map(outgoing.foldLeft(_) { (req, filter) => filter(req) })
        .map { req => effectiveRequest = req; req }
        .map(conn.send)
        .map(addAttributes(_, conn, correlate, target))
        .map(addRequestAttribute(_, effectiveRequest))
        .map(persistCookies(target, _))
        .map(incoming.foldLeft(_) { (res, filter) => filter(res) })
        .map(persistConnection(handler, secure, host, port, _))
        .get
    finally
      Try(conn.close())

  private def getEffectiveConnection(req: HttpRequest): String =
    req.target.scheme.matches("wss?") match
      case true  => WebSocket.validate(req).connection.mkString(", ")
      case false =>
        req.connectionOption
          .orElse(Some(Nil))
          .map { values => values.filterNot(_.matches("(?i)close|keep-alive|TE")) }
          .map { values => if req.hasTE then values :+ "TE" else values }
          .map { _ :+ (if shouldKeepAlive(req) then "keep-alive" else "close") }
          .map(_.mkString(", "))
          .get

  private def toBodilessRequest(req: HttpRequest): HttpRequest =
    req.setBody(Entity.empty).contentLengthRemoved.transferEncodingRemoved

  private def toBodyRequest(req: HttpRequest): HttpRequest =
    req.transferEncodingOption.map { encoding =>
      req.setTransferEncoding(encoding.filterNot(_.isChunked) :+ TransferCoding("chunked"))
        .contentLengthRemoved
    }.orElse {
      req.contentLengthOption.map {
        case 0          => req.setBody(Entity.empty)
        case n if n > 0 => req
        case length     => throw RequestAborted(s"Invalid Content-Length: $length")
      }
    }.orElse {
      req.body.knownSize.collect {
        case 0          => req.setBody(Entity.empty).setContentLength(0)
        case n if n > 0 => req.setContentLength(n)
      }
    }.getOrElse {
      req.setTransferEncoding(TransferCoding("chunked"))
    }

  private def shouldKeepAlive(req: HttpRequest): Boolean =
    keepAlive && !req.connection.exists("close".equalsIgnoreCase)

  private def shouldKeepAlive(res: HttpResponse): Boolean =
    keepAlive && !res.connection.exists("close".equalsIgnoreCase) &&
      (res.isSuccessful || res.statusCode == 304) &&
      res.getAttribute[HttpClientConnection]("scamper.http.client.message.connection")
        .forall(_.getManaged())

  private def getClientConnection(secure: Boolean, host: String, port: Int): HttpClientConnection =
    ConnectionManager.get(secure, host, port).getOrElse {
      val factory = getSocketFactory(secure)
      val socket  = factory.createSocket(host, port)

      HttpClientConnection(socket)
    }

  private def getSocketFactory(secure: Boolean): SocketFactory =
    if secure then secureSocketFactory else SocketFactory.getDefault()

  private def createCorrelate(requestId: Long): String =
    f"${System.currentTimeMillis}%x-$id%04x-$requestId%04x"

  private def addAttributes[T <: HttpMessage & MessageBuilder[T]](msg: T, conn: HttpClientConnection, correlate: String, absoluteTarget: Uri): T =
    msg.putAttributes(
      "scamper.http.client.message.client"         -> this,
      "scamper.http.client.message.connection"     -> conn,
      "scamper.http.client.message.socket"         -> conn.getSocket(),
      "scamper.http.client.message.correlate"      -> correlate,
      "scamper.http.client.message.absoluteTarget" -> absoluteTarget
    )

  private def addRequestAttribute(res: HttpResponse, req: HttpRequest): HttpResponse =
    res.putAttributes("scamper.http.client.response.request" -> req)

  private def addAccept(req: HttpRequest): HttpRequest =
    (req.hasAccept || accept.isEmpty) match
      case true  => req
      case false => req.setAccept(accept)

  private def addAcceptEncoding(req: HttpRequest): HttpRequest =
    (req.hasAcceptEncoding || acceptEncoding.isEmpty) match
      case true  => req
      case false => req.setAcceptEncoding(acceptEncoding)

  private def persistCookies(target: Uri, res: HttpResponse): HttpResponse =
    res.getHeaderValues("Set-Cookie")
      .flatMap { value => Try(SetCookie.parse(value)).toOption }
      .foreach { cookie => cookies.put(target, cookie) }
    res

  private def persistConnection[T](handler: ResponseHandler[T], secure: Boolean, host: String, port: Int, res: HttpResponse): T =
    val result = handler(res)

    if shouldKeepAlive(res) then
      res.getAttribute[HttpClientConnection]("scamper.http.client.message.connection")
        .foreach(ConnectionManager.add(secure, host, port, _))

    result

  private def setCloseGuard(msg: HttpMessage, enabled: Boolean): Unit =
    msg.getAttribute[HttpClientConnection]("scamper.http.client.message.connection")
      .map(_.setCloseGuard(enabled))
      .getOrElse(throw new NoSuchElementException("No such attribute: scamper.http.client.message.connection"))
