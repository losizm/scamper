/*
 * Copyright 2021 Carlos Conyers
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

import scamper.http.cookies.{ CookieStore, PlainCookie, RequestCookies, SetCookie }
import scamper.http.headers.{ Accept, AcceptEncoding, Connection, ContentLength, Host, TE, TransferEncoding, Upgrade }
import scamper.http.types.{ ContentCodingRange, MediaRange, TransferCoding }
import scamper.http.websocket.*

import Auxiliary.UriType
import RequestMethod.Registry.*
import Validate.notNull

private object HttpClientImpl:
  private val count = AtomicLong(0)

  case class Settings(
    accept:              Seq[MediaRange] = Seq(MediaRange("*/*")),
    acceptEncoding:      Seq[ContentCodingRange] = Nil,
    bufferSize:          Int = 8192,
    readTimeout:         Int = 30000,
    continueTimeout:     Int = 1000,
    cookies:             CookieStore = CookieStore.Null,
    outgoing:            Seq[RequestFilter] = Nil,
    incoming:            Seq[ResponseFilter] = Nil,
    secureSocketFactory: SSLSocketFactory = SSLSocketFactory.getDefault().asInstanceOf[SSLSocketFactory]
  )

  def apply(settings: Settings): HttpClientImpl =
    new HttpClientImpl(count.incrementAndGet, settings)

private class HttpClientImpl(id: Long, settings: HttpClientImpl.Settings) extends HttpClient:
  val accept          = settings.accept
  val acceptEncoding  = settings.acceptEncoding
  val bufferSize      = settings.bufferSize.max(1024)
  val readTimeout     = settings.readTimeout.max(0)
  val continueTimeout = settings.continueTimeout.max(0)
  val cookies         = settings.cookies

  private val outgoing = settings.outgoing
  private val incoming = settings.incoming

  private val secureSocketFactory = settings.secureSocketFactory
  private val requestCount        = AtomicLong(0)

  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T =
    notNull(handler)

    val target = request.target

    require(target.isAbsolute, s"Request target not absolute: $target")
    require(target.getScheme.matches("(http|ws)s?"), s"Unsupported scheme: ${target.getScheme}")

    val secure     = target.getScheme.matches("https|wss")
    val host       = getEffectiveHost(target)
    val userAgent  = request.getHeaderValueOrElse("User-Agent", "Scamper/31.0.0")
    val reqCookies = request.cookies ++ cookies.get(target)
    val connection = target.getScheme.matches("wss?") match
      case true  => WebSocket.validate(request).connection.mkString(", ")
      case false => getEffectiveConnection(request)

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
      Header("Host", host) +:
      Header("User-Agent", userAgent) +:
      effectiveRequest.headers.filterNot(_.name.matches("(?i)Host|User-Agent|Cookie|Connection")) :+
      Header("Connection", connection)
    ).setCookies(reqCookies)

    effectiveRequest = effectiveRequest.setTarget(target.toTarget)

    if !effectiveRequest.path.startsWith("/") && effectiveRequest.path != "*" then
      effectiveRequest = effectiveRequest.setPath("/" + effectiveRequest.path)

    val conn = createClientConnection(
      secure match
        case true  => secureSocketFactory
        case false => SocketFactory.getDefault()
      ,
      target.getHost,
      target.getPort match
        case -1   => if secure then 443 else 80
        case port => port
    )

    try
      val correlate = createCorrelate(requestCount.incrementAndGet)

      Try(addAttributes(effectiveRequest, conn, correlate, target))
        .map(addAccept)
        .map(addAcceptEncoding)
        .map(outgoing.foldLeft(_) { (req, filter) => filter(req) })
        .map { req => effectiveRequest = req; req }
        .map(conn.send)
        .map(addAttributes(_, conn, correlate, target))
        .map(addRequestAttribute(_, effectiveRequest))
        .map(storeCookies(target, _))
        .map(incoming.foldLeft(_) { (res, filter) => filter(res) })
        .map(handler.apply)
        .get
    finally Try(conn.close())

  def get[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(Get, target, headers, cookies, Entity.empty)(handler)

  def post[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(Post, target, headers, cookies, body)(handler)

  def put[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(Put, target, headers, cookies, body)(handler)

  def delete[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(Delete, target, headers, cookies, Entity.empty)(handler)

  def websocket[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (app: WebSocketApplication[T]): T =

    require(target.getScheme == "ws" || target.getScheme == "wss", s"Invalid WebSocket scheme: ${target.getScheme}")

    val req = HttpRequest(
      Get,
      target,
      Header("Upgrade", "websocket") +:
      Header("Connection", "Upgrade") +:
      Header("Sec-WebSocket-Key", WebSocket.generateKey()) +:
      Header("Sec-WebSocket-Version", "13") +:
      Header("Sec-WebSocket-Extensions", "permessage-deflate; client_no_context_takeover; server_no_context_takeover") +:
      headers
    ).setCookies(cookies)

    send(req) { res =>
      WebSocket.checkHandshake(req, res) match
        case true =>
          val session = WebSocketSession.forClient(
            res.socket,
            res.correlate,
            res.absoluteTarget,
            req.secWebSocketVersion,
            WebSocket.enablePermessageDeflate(res),
            None
          )
          setCloseGuard(res, true)
          try app(session)
          catch case cause: Throwable =>
            setCloseGuard(res, false)
            throw cause

        case false => throw WebSocketHandshakeFailure(s"Connection upgrade not accepted: ${res.status}")
      }

  private def send[T](method: RequestMethod, target: Uri, headers: Seq[Header], cookies: Seq[PlainCookie], body: Entity)
      (handler: ResponseHandler[T]): T =
    val req = cookies match
      case Nil => HttpRequest(method, target, headers, body)
      case _   => HttpRequest(method, target, headers, body).setCookies(cookies)

    send(req)(handler)

  private def getEffectiveHost(target: Uri): String =
    target.getPort match
      case -1   => target.getHost
      case port => target.getHost + ":" + port

  private def getEffectiveConnection(request: HttpRequest): String =
    request.getConnection
      .orElse(Some(Nil))
      .map { values => values.filterNot(_.matches("(?i)close|keep-alive|TE")) }
      .map { values => if request.hasTE then values :+ "TE" else values }
      .map(_ :+ "close")
      .map(_.mkString(", "))
      .get

  private def createClientConnection(factory: SocketFactory, host: String, port: Int): HttpClientConnection =
    val socket = factory.createSocket(host, port)

    try
      socket.setSoTimeout(readTimeout)
      socket.setSendBufferSize(bufferSize)
      socket.setReceiveBufferSize(bufferSize)
    catch case cause: Throwable =>
      Try(socket.close())
      throw cause

    HttpClientConnection(socket, bufferSize, continueTimeout)

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

  private def setCloseGuard(msg: HttpMessage, enabled: Boolean): Unit =
    msg.getAttribute[HttpClientConnection]("scamper.http.client.message.connection")
      .map(_.setCloseGuard(enabled))
      .getOrElse(throw new NoSuchElementException("No such attribute: scamper.http.client.message.connection"))

  private def addAccept(req: HttpRequest): HttpRequest =
    (req.hasAccept || accept.isEmpty) match
      case true  => req
      case false => req.setAccept(accept)

  private def addAcceptEncoding(req: HttpRequest): HttpRequest =
    (req.hasAcceptEncoding || acceptEncoding.isEmpty) match
      case true  => req
      case false => req.setAcceptEncoding(acceptEncoding)

  private def storeCookies(target: Uri, res: HttpResponse): HttpResponse =
    res.getHeaderValues("Set-Cookie")
      .flatMap { value => Try(SetCookie.parse(value)).toOption }
      .foreach { cookie => cookies.put(target, cookie) }
    res

  private def toBodilessRequest(request: HttpRequest): HttpRequest =
    request.setBody(Entity.empty).removeContentLength.removeTransferEncoding

  private def toBodyRequest(request: HttpRequest): HttpRequest =
    request.getTransferEncoding.map { encoding =>
      request.setTransferEncoding(encoding.filterNot(_.isChunked) :+ TransferCoding("chunked"))
        .removeContentLength
    }.orElse {
      request.getContentLength.map {
        case 0          => request.setBody(Entity.empty)
        case n if n > 0 => request
        case length     => throw RequestAborted(s"Invalid Content-Length: $length")
      }
    }.orElse {
      request.body.knownSize.collect {
        case 0          => request.setBody(Entity.empty).setContentLength(0)
        case n if n > 0 => request.setContentLength(n)
      }
    }.getOrElse {
      request.setTransferEncoding(TransferCoding("chunked"))
    }
