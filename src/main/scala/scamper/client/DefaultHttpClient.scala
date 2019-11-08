/*
 * Copyright 2019 Carlos Conyers
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

import javax.net.SocketFactory
import javax.net.ssl.{ SSLSocketFactory, TrustManager }

import scala.util.Try
import scala.util.control.NonFatal

import scamper.{ Entity, Header, HttpRequest, ListParser, RequestMethod, Uri }
import scamper.Auxiliary.UriType
import scamper.RequestMethod.Registry._
import scamper.cookies.{ PlainCookie, RequestCookies }
import scamper.headers.{ Connection, ContentLength, Host, TE, TransferEncoding }
import scamper.types.TransferCoding

import HttpClient.{ InboundFilter, OutboundFilter }

private object DefaultHttpClient {
  case class Settings(
    bufferSize: Int,
    readTimeout: Int,
    continueTimeout: Int,
    incoming: Seq[InboundFilter],
    outgoing: Seq[OutboundFilter]
  )

  def apply(settings: Settings): DefaultHttpClient = {
    val factory = SSLSocketFactory.getDefault().asInstanceOf[SSLSocketFactory]
    new DefaultHttpClient(settings, factory)
  }

  def apply(settings: Settings, truststore: File): DefaultHttpClient = {
    val factory = SecureSocketFactory.create(truststore)
    new DefaultHttpClient(settings, factory)
  }

  def apply(settings: Settings, trustManager: TrustManager): DefaultHttpClient = {
    val factory = SecureSocketFactory.create(trustManager)
    new DefaultHttpClient(settings, factory)
  }
}

private class DefaultHttpClient private (settings: DefaultHttpClient.Settings, secureSocketFactory: SSLSocketFactory) extends HttpClient {
  val bufferSize = settings.bufferSize.max(1024)
  val readTimeout = settings.readTimeout.max(0)
  val continueTimeout = settings.continueTimeout.max(0)

  private val incoming = settings.incoming
  private val outgoing = settings.outgoing

  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T = {
    val target = request.target

    require(target.isAbsolute, s"Request target not absolute: $target")
    require(target.getScheme.matches("http(s)?"), s"Invalid scheme: ${target.getScheme}")

    val secure = target.getScheme == "https"
    val host = getEffectiveHost(target)
    val userAgent = request.getHeaderValueOrElse("User-Agent", "Scamper/11.0.0")
    val connection = getEffectiveConnection(request)

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

    effectiveRequest = effectiveRequest.withHeaders({
      Header("Host", host) +:
      Header("User-Agent", userAgent) +:
      effectiveRequest.headers.filterNot(_.name.matches("(?i)Host|User-Agent|Connection")) :+
      Header("Connection", connection)
    } : _*)

    effectiveRequest = effectiveRequest.withTarget(Uri(target.toURL.getFile))

    if (! effectiveRequest.path.startsWith("/") && effectiveRequest.path != "*")
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
      val req = outgoing.foldLeft(effectiveRequest) { (req, filter) => filter(req) }
      val res = incoming.foldLeft(conn.send(req))   { (res, filter) => filter(res) }
      handler(res)
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

  private def send[T](method: RequestMethod, target: Uri, headers: Seq[Header], cookies: Seq[PlainCookie], body: Entity)(handler: ResponseHandler[T]): T = {
    val req = cookies match {
      case Nil => HttpRequest(method, target, headers, body)
      case _   => HttpRequest(method, target, headers, body).withCookies(cookies : _*)
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
      case NonFatal(cause) =>
        Try(socket.close())
        throw cause
    }

    new HttpClientConnection(socket, bufferSize, continueTimeout)
  }

  private def toBodilessRequest(request: HttpRequest): HttpRequest =
    request.withBody(Entity.empty).removeContentLength().removeTransferEncoding()

  private def toBodyRequest(request: HttpRequest): HttpRequest =
    request.getTransferEncoding.map { encoding =>
      request.withTransferEncoding(encoding.filterNot(_.isChunked) :+ TransferCoding("chunked") : _*).removeContentLength
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
