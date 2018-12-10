/*
 * Copyright 2018 Carlos Conyers
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
import javax.net.ssl.SSLSocketFactory

import scala.util.Try
import scala.util.control.NonFatal

import scamper.{ Entity, Header, HttpException, HttpRequest, RequestMethod }
import scamper.RequestMethods._
import scamper.auxiliary.UriType
import scamper.cookies.{ PlainCookie, RequestCookies }
import scamper.headers.{ ContentLength, Host, TransferEncoding }
import scamper.types.TransferCoding

private object DefaultHttpClient {
  def apply(bufferSize: Int, timeout: Int): DefaultHttpClient = {
    implicit val factory = SSLSocketFactory.getDefault().asInstanceOf[SSLSocketFactory]
    new DefaultHttpClient(bufferSize, timeout)
  }

  def apply(bufferSize: Int, timeout: Int, trustStore: File): DefaultHttpClient = {
    implicit val factory = SecureSocketFactory.create(trustStore)
    new DefaultHttpClient(bufferSize, timeout)
  }
}

private class DefaultHttpClient private (val bufferSize: Int, val timeout: Int)(implicit secureSocketFactory: SSLSocketFactory) extends HttpClient {
  def send[T](request: HttpRequest, secure: Boolean = false)(handler: ResponseHandler[T]): T = {
    val scheme = if (secure) "https" else "http"
    val host = getEffectiveHost(request.target, request.getHost)
    val target = request.target.withScheme(scheme).withAuthority(host)
    val userAgent = request.getHeaderValueOrElse("User-Agent", "Scamper/2.0")

    var effectiveRequest = request.method match {
      case GET     => toBodilessRequest(request)
      case POST    => toBodyRequest(request)
      case PUT     => toBodyRequest(request)
      case PATCH   => toBodyRequest(request)
      case DELETE  => toBodilessRequest(request)
      case HEAD    => toBodilessRequest(request)
      case TRACE   => toBodilessRequest(request)
      case OPTIONS => toBodyRequest(request)
      case CONNECT => toBodilessRequest(request)
    }

    effectiveRequest = effectiveRequest.withHeaders({
      Header("Host", host) +:
      Header("User-Agent", userAgent) +:
      effectiveRequest.headers.filterNot(header => header.name.matches("(?i)Host|User-Agent"))
    } : _*)

    effectiveRequest = effectiveRequest.withTarget(new URI(target.toURL.getFile))

    if (! effectiveRequest.path.startsWith("/"))
      effectiveRequest = effectiveRequest.withPath("/" + effectiveRequest.path)

    val conn = getConnection(
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
    (handler: ResponseHandler[T]): T = send(GET, target, headers, cookies, Entity.empty())(handler)

  def post[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty())
    (handler: ResponseHandler[T]): T = send(POST, target, headers, cookies, body)(handler)

  def put[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty())
    (handler: ResponseHandler[T]): T = send(PUT, target, headers, cookies, body)(handler)

  def patch[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty())
    (handler: ResponseHandler[T]): T = send(PATCH, target, headers, cookies, body)(handler)

  def delete[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(DELETE, target, headers, cookies, Entity.empty())(handler)

  def head[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)
    (handler: ResponseHandler[T]): T = send(HEAD, target, headers, cookies, Entity.empty())(handler)

  def options[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty())
    (handler: ResponseHandler[T]): T = send(OPTIONS, target, headers, cookies, body)(handler)

  def trace[T](target: URI, headers: Seq[Header] = Nil)
    (handler: ResponseHandler[T]): T = send(TRACE, target, headers, Nil, Entity.empty())(handler)

  private def send[T](method: RequestMethod, target: URI, headers: Seq[Header], cookies: Seq[PlainCookie], body: Entity)(handler: ResponseHandler[T]): T = {
    if (!target.isAbsolute)
      throw RequestAborted(s"Target is not absolute: $target")

    if (target.getScheme != "http" && target.getScheme != "https")
      throw RequestAborted(s"Invalid target scheme: ${target.getScheme}")

    val req = cookies match {
      case Nil => HttpRequest(method, target, headers, body)
      case _   => HttpRequest(method, target, headers, body).withCookies(cookies : _*)
    }

    send(req, target.getScheme == "https")(handler)
  }

  private def getEffectiveHost(target: URI, default: => Option[String]): String =
    target.getHost match {
      case null => default.getOrElse(throw RequestAborted("Cannot determine host"))
      case host => target.getPort match {
        case -1   => host
        case port => s"$host:$port"
      }
    }

  private def getConnection(factory: SocketFactory, host: String, port: Int): HttpClientConnection = {
    val socket = factory.createSocket(host, port)

    try {
      socket.setSoTimeout(timeout)
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
    request.withBody(Entity.empty).removeContentLength.removeTransferEncoding

  private def toBodyRequest(request: HttpRequest): HttpRequest =
    request.getContentLength.map {
      case 0          => request.withBody(Entity.empty).removeTransferEncoding
      case n if n > 0 => request.removeTransferEncoding
      case length     => throw RequestAborted(s"Invalid Content-Length: $length")
    }.orElse {
      request.getTransferEncoding.map(_ => request)
    }.orElse {
      request.body.getLength.collect {
        case 0          => request.withBody(Entity.empty).withContentLength(0)
        case n if n > 0 => request.withContentLength(n)
      }
    }.getOrElse {
      request.withTransferEncoding(TransferCoding("chunked"))
    }
}