/*
 * Copyright 2017-2020 Carlos Conyers
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
package scamper.client

import java.io.File
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong

import javax.net.SocketFactory
import javax.net.ssl.{ SSLSocketFactory, TrustManager }

import scala.util.Try

import scamper._
import scamper.Auxiliary.UriType
import scamper.RequestMethod.Registry._
import scamper.Validate.notNull
import scamper.client.Implicits.ClientHttpMessageType
import scamper.cookies.{ CookieStore, PlainCookie, RequestCookies, SetCookie }
import scamper.headers.{ AcceptEncoding, Connection, ContentLength, Host, TE, TransferEncoding, Upgrade }
import scamper.types.{ ContentCodingRange, TransferCoding }
import scamper.websocket._

private object HttpClientImpl {
  private val count = new AtomicLong(0)

  case class Settings(
    acceptEncodings: Seq[ContentCodingRange] = Nil,
    bufferSize: Int = 8192,
    readTimeout: Int = 30000,
    continueTimeout: Int = 1000,
    cookieStore: CookieStore = CookieStore.noop(),
    outgoing: Seq[RequestFilter] = Nil,
    incoming: Seq[ResponseFilter] = Nil,
    secureSocketFactory: SSLSocketFactory = SSLSocketFactory.getDefault().asInstanceOf[SSLSocketFactory]
  )

  def apply(settings: Settings): HttpClientImpl = new HttpClientImpl(count.incrementAndGet, settings)
}

private class HttpClientImpl(id: Long, settings: HttpClientImpl.Settings) extends HttpClient {
  val acceptEncodings = settings.acceptEncodings
  val bufferSize = settings.bufferSize.max(1024)
  val readTimeout = settings.readTimeout.max(0)
  val continueTimeout = settings.continueTimeout.max(0)
  val cookieStore = settings.cookieStore

  private val outgoing = settings.outgoing
  private val incoming = settings.incoming

  private val secureSocketFactory = settings.secureSocketFactory
  private val requestCount = new AtomicLong(0)

  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T = {
    notNull(handler)

    val target = request.target

    require(target.isAbsolute, s"Request target not absolute: $target")
    require(target.getScheme.matches("(http|ws)s?"), s"Invalid scheme: ${target.getScheme}")

    val secure = target.getScheme.matches("https|wss")
    val host = getEffectiveHost(target)
    val userAgent = request.getHeaderValueOrElse("User-Agent", "Scamper/13.2.1")
    val cookies = request.cookies ++ cookieStore.get(target)
    val connection = target.getScheme.matches("wss?") match {
      case true  => checkWebSocketRequest(request).connection.mkString(", ")
      case false => getEffectiveConnection(request)
    }

    var effectiveRequest = request.method match {
      case GET     => toBodilessRequest(request)
      case POST    => toBodyRequest(request)
      case PUT     => toBodyRequest(request)
      case PATCH   => toBodyRequest(request)
      case DELETE  => toBodilessRequest(request)
      case HEAD    => toBodilessRequest(request)
      case TRACE   => toBodilessRequest(request)
      case OPTIONS => toBodyRequest(request)
      case _       => request
    }

    effectiveRequest = effectiveRequest.withHeaders(
      Header("Host", host) +:
      Header("User-Agent", userAgent) +:
      effectiveRequest.headers.filterNot(_.name.matches("(?i)Host|User-Agent|Cookie|Connection")) :+
      Header("Connection", connection)
    ).withCookies(cookies)

    effectiveRequest = effectiveRequest.withTarget(target.toTarget)

    if (!effectiveRequest.path.startsWith("/") && effectiveRequest.path != "*")
      effectiveRequest = effectiveRequest.withPath("/" + effectiveRequest.path)

    val conn = createClientConnection(
      if (secure) secureSocketFactory else SocketFactory.getDefault(),
      target.getHost,
      target.getPort match {
        case -1   => if (secure) 443 else 80
        case port => port
      }
    )

    try {
      val correlate = createCorrelate(requestCount.incrementAndGet)

      Try(addAttributes(effectiveRequest, conn, correlate, target))
        .map(addAcceptEncoding)
        .map(outgoing.foldLeft(_) { (req, filter) => filter(req) })
        .map(conn.send)
        .map(addAttributes(_, conn, correlate, target))
        .map(storeCookies(target, _))
        .map(incoming.foldLeft(_) { (res, filter) => filter(res) })
        .map(handler.apply)
        .get
    } finally Try(conn.close())
  }

  def get[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(GET, target, headers, cookies, Entity.empty)(handler)

  def post[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(POST, target, headers, cookies, body)(handler)

  def put[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(PUT, target, headers, cookies, body)(handler)

  def patch[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(PATCH, target, headers, cookies, body)(handler)

  def delete[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(DELETE, target, headers, cookies, Entity.empty)(handler)

  def head[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(HEAD, target, headers, cookies, Entity.empty)(handler)

  def options[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(OPTIONS, target, headers, cookies, body)(handler)

  def trace[T](target: Uri, headers: Seq[Header] = Nil)
    (handler: ResponseHandler[T]): T = send(TRACE, target, headers, Nil, Entity.empty)(handler)

  def websocket[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: WebSocketSession => T): T = {

    require(target.getScheme == "ws" || target.getScheme == "wss", s"Invalid WebSocket scheme: ${target.getScheme}")

    val req = HttpRequest(
      GET,
      target,
      Header("Upgrade", "websocket") +:
      Header("Connection", "Upgrade") +:
      Header("Sec-WebSocket-Key", generateWebSocketKey()) +:
      Header("Sec-WebSocket-Version", "13") +:
      headers
    ).withCookies(cookies)

    send(req) { res =>
      checkWebSocketHandshake(req, res) match {
        case true =>
          val session = WebSocketSession.forClient(
            res.socket,
            res.correlate,
            res.absoluteTarget,
            req.secWebSocketVersion,
            None
          )
          setCloseGuard(res, true)
          try handler(session)
          catch {
            case cause: Throwable =>
              setCloseGuard(res, false)
              throw cause
          }
        case false => throw WebSocketHandshakeFailure(s"Connection upgrade not accepted: ${res.status}")
      }
    }
  }

  private def send[T](method: RequestMethod, target: Uri, headers: Seq[Header], cookies: Seq[PlainCookie], body: Entity)(handler: ResponseHandler[T]): T = {
    val req = cookies match {
      case Nil => HttpRequest(method, target, headers, body)
      case _   => HttpRequest(method, target, headers, body).withCookies(cookies)
    }

    send(req)(handler)
  }

  private def getEffectiveHost(target: Uri): String =
    target.getPort match {
      case -1   => target.getHost
      case port => target.getHost + ":" + port
    }

  private def getEffectiveConnection(request: HttpRequest): String =
    request.getConnection
      .orElse(Some(Nil))
      .map { values => values.filterNot(_.matches("(?i)close|keep-alive|TE")) }
      .map { values => if (request.hasTE) values :+ "TE" else values }
      .map(_ :+ "close")
      .map(_.mkString(", "))
      .get

  private def createClientConnection(factory: SocketFactory, host: String, port: Int): HttpClientConnection = {
    val socket = factory.createSocket(host, port)

    try {
      socket.setSoTimeout(readTimeout)
      socket.setSendBufferSize(bufferSize)
      socket.setReceiveBufferSize(bufferSize)
    } catch {
      case cause: Throwable =>
        Try(socket.close())
        throw cause
    }

    new HttpClientConnection(socket, bufferSize, continueTimeout)
  }

  private def createCorrelate(requestId: Long): String =
    f"${System.currentTimeMillis}%x-$id%04x-$requestId%04x"

  private def addAttributes[T <: HttpMessage](msg: T, conn: HttpClientConnection, correlate: String, absoluteTarget: Uri)(implicit ev: <:<[T, MessageBuilder[T]]): T =
    msg
      .withAttribute("scamper.client.message.connection"     -> conn)
      .withAttribute("scamper.client.message.socket"         -> conn.getSocket())
      .withAttribute("scamper.client.message.correlate"      -> correlate)
      .withAttribute("scamper.client.message.absoluteTarget" -> absoluteTarget)

  private def setCloseGuard(msg: HttpMessage, enabled: Boolean): Unit =
    msg.getAttribute[HttpClientConnection]("scamper.client.message.connection")
      .map(_.setCloseGuard(enabled))
      .getOrElse(throw new NoSuchElementException("No such attribute: scamper.client.message.connection"))

  private def addAcceptEncoding(req: HttpRequest): HttpRequest =
    req.hasAcceptEncoding match {
      case true  => req
      case false =>
        acceptEncodings.isEmpty match {
          case true  => req
          case false => req.withAcceptEncoding(acceptEncodings)
        }
    }

  private def storeCookies(target: Uri, res: HttpResponse): HttpResponse = {
    res.getHeaderValues("Set-Cookie")
      .flatMap { value => Try(SetCookie.parse(value)).toOption }
      .foreach { cookie => cookieStore.put(target, cookie) }
    res
  }

  private def toBodilessRequest(request: HttpRequest): HttpRequest =
    request.withBody(Entity.empty).removeContentLength().removeTransferEncoding()

  private def toBodyRequest(request: HttpRequest): HttpRequest =
    request.getTransferEncoding.map { encoding =>
      request.withTransferEncoding(encoding.filterNot(_.isChunked) :+ TransferCoding("chunked"))
        .removeContentLength
    }.orElse {
      request.getContentLength.map {
        case 0          => request.withBody(Entity.empty)
        case n if n > 0 => request
        case length     => throw RequestAborted(s"Invalid Content-Length: $length")
      }
    }.orElse {
      request.body.getLength.collect {
        case 0          => request.withBody(Entity.empty).withContentLength(0)
        case n if n > 0 => request.withContentLength(n)
      }
    }.getOrElse {
      request.withTransferEncoding(TransferCoding("chunked"))
    }
}
