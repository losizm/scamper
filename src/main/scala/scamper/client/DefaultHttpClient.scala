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
import java.net.URI

import javax.net.SocketFactory
import javax.net.ssl.{ SSLSocketFactory, TrustManager }

import scala.util.Try
import scala.util.control.NonFatal

import scamper.{ Entity, Header, HttpRequest, ListParser, RequestMethod }
import scamper.Auxiliary.UriType
import scamper.RequestMethod.Registry._
import scamper.cookies.{ PlainCookie, RequestCookies }
import scamper.headers.{ Connection, ContentLength, Host, TE, TransferEncoding }
import scamper.types.TransferCoding

private object DefaultHttpClient {
  def apply(bufferSize: Int, readTimeout: Int): DefaultHttpClient = {
    implicit val factory = SSLSocketFactory.getDefault().asInstanceOf[SSLSocketFactory]
    new DefaultHttpClient(bufferSize, readTimeout)
  }

  def apply(bufferSize: Int, readTimeout: Int, trustStore: File): DefaultHttpClient = {
    implicit val factory = SecureSocketFactory.create(trustStore)
    new DefaultHttpClient(bufferSize, readTimeout)
  }

  def apply(bufferSize: Int, readTimeout: Int, trustManager: TrustManager): DefaultHttpClient = {
    implicit val factory = SecureSocketFactory.create(trustManager)
    new DefaultHttpClient(bufferSize, readTimeout)
  }
}

private class DefaultHttpClient private (val bufferSize: Int, val readTimeout: Int)(implicit secureSocketFactory: SSLSocketFactory) extends HttpClient {
  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T = {
    val target = request.target

    require(target.isAbsolute, s"Request target not absolute: $target")
    require(target.getScheme.matches("http(s)?"), s"Invalid scheme: ${target.getScheme}")

    val secure = target.getScheme == "https"
    val host = getEffectiveHost(target)
    val userAgent = request.getHeaderValueOrElse("User-Agent", "Scamper/10.0.1")
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
      effectiveRequest.headers.filterNot(header => header.name.matches("(?i)Host|User-Agent|Connection")) :+
      Header("Connection", connection)
    } : _*)

    effectiveRequest = effectiveRequest.withTarget(new URI(target.toURL.getFile))

    if (! effectiveRequest.path.startsWith("/") && effectiveRequest.path != "*")
      effectiveRequest = effectiveRequest.withPath("/" + effectiveRequest.path)

    val conn = getClientConnection(
      if (secure) secureSocketFactory else SocketFactory.getDefault(),
      target.getHost,
      target.getPort match {
        case -1   => if (secure) 443 else 80
        case port => port
      }
    )

    try handler(conn.send(effectiveRequest))
    finally Try(conn.close())
  }

  def get[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(GET, target, headers, cookies, Entity.empty)(handler)

  def post[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(POST, target, headers, cookies, body)(handler)

  def put[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(PUT, target, headers, cookies, body)(handler)

  def patch[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(PATCH, target, headers, cookies, body)(handler)

  def delete[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(DELETE, target, headers, cookies, Entity.empty)(handler)

  def head[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(HEAD, target, headers, cookies, Entity.empty)(handler)

  def options[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T = send(OPTIONS, target, headers, cookies, body)(handler)

  def trace[T](target: URI, headers: Seq[Header] = Nil)
    (handler: ResponseHandler[T]): T = send(TRACE, target, headers, Nil, Entity.empty)(handler)

  private def send[T](method: RequestMethod, target: URI, headers: Seq[Header], cookies: Seq[PlainCookie], body: Entity)(handler: ResponseHandler[T]): T = {
    val req = cookies match {
      case Nil => HttpRequest(method, target, headers, body)
      case _   => HttpRequest(method, target, headers, body).withCookies(cookies : _*)
    }

    send(req)(handler)
  }

  private def getEffectiveHost(target: URI): String =
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

  private def getClientConnection(factory: SocketFactory, host: String, port: Int): HttpClientConnection = {
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

    new HttpClientConnection(socket)
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
