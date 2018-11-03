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
package scamper

import java.net.URI

import scala.util.Try

import types.TransferCoding

import ImplicitExtensions.HttpUriType
import ImplicitHeaders.{ ContentLength, Host, TransferEncoding }
import RequestMethods._

/** HTTP client */
object HttpClient {
  /**
   * Sends request and passes response to supplied handler.
   *
   * To make effective use of this method, either the Host header must be set,
   * or the request target must be an absolute URI. Also note that if the
   * request target is absolute, its scheme is overridden in accordance to
   * {@code secure}.
   *
   * @param request HTTP request
   * @param secure specifies whether to use HTTPS protocol
   * @param handler response handler
   *
   * @return value from supplied handler
   */
  def send[T](request: HttpRequest, secure: Boolean = false)(handler: HttpResponse => T): T = {
    val scheme = if (secure) "https" else "http"
    val host = getHost(request.target.getRawAuthority, request.getHost)
    val target = request.target.withScheme(scheme).withAuthority(host)
    val userAgent = getUserAgent(request.getHeaderValue("User-Agent"))

    var effectiveRequest = request.method match {
      case GET     => getNoBodyRequest(request)
      case POST    => getBodyRequest(request)
      case PUT     => getBodyRequest(request)
      case DELETE  => getNoBodyRequest(request)
      case HEAD    => getNoBodyRequest(request)
      case TRACE   => getNoBodyRequest(request)
      case OPTIONS => getBodyRequest(request)
      case CONNECT => getNoBodyRequest(request)
    }

    effectiveRequest = effectiveRequest.withHeaders({
      Header("Host", host) +:
      Header("User-Agent", userAgent) +:
      effectiveRequest.headers.filterNot(header => header.name.matches("(?i)Host|User-Agent"))
    } : _*)

    if (! effectiveRequest.path.startsWith("/"))
      effectiveRequest = effectiveRequest.withPath("/" + effectiveRequest.path)

    val conn = HttpClientConnection(target.getHost, getPort(target.getPort, secure), secure)
    try handler(conn.send(effectiveRequest))
    finally Try(conn.close())
  }

  private def getUserAgent(product: Option[String]): String =
    product.getOrElse(s"Java/${sys.props("java.version")} Scamper/0.12")

  private def getHost(authority: String, default: => Option[String]): String =
    Option(authority).orElse(default).getOrElse(throw new HttpException("Cannot determine host"))

  private def getPort(port: Int, secure: Boolean): Int =
   port match {
      case -1 => if (secure) 443 else 80
      case _  => port
    }

  private def getNoBodyRequest(request: HttpRequest): HttpRequest =
    request.withBody(Entity.empty).removeContentLength.removeTransferEncoding

  private def getBodyRequest(request: HttpRequest): HttpRequest =
    request.getContentLength.map {
      case 0          => request.withBody(Entity.empty).removeTransferEncoding
      case n if n > 0 => request.removeTransferEncoding
      case length     => throw new HttpException(s"Invalid Content-Length: $length")
    }.orElse {
      request.getTransferEncoding.map(_ => request)
    }.orElse {
      request.body.length.collect {
        case 0          => request.withBody(Entity.empty).withContentLength(0)
        case n if n > 0 => request.withContentLength(n)
      }
    }.getOrElse {
      request.withTransferEncoding(TransferCoding("chunked"))
    }
}
