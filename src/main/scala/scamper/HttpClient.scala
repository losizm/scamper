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

import ImplicitExtensions.{ HttpStringType, HttpUriType }

/** HTTP client */
object HttpClient {
  /**
   * Sends request and passes response to supplied handler.
   *
   * To make effective use of this method, either the Host header must be set,
   * or the request URI must be absolute. Also note that if the request URI is
   * absolute, its scheme is overridden in accordance to {@code secure}.
   *
   * @param request HTTP request
   * @param secure specifies whether to use HTTPS protocol
   * @param handler response handler
   *
   * @return value from supplied handler
   */
  def send[T](request: HttpRequest, secure: Boolean = false)(handler: HttpResponse => T): T = {
    val scheme = if (secure) "https" else "http"
    val uri = request.uri.toURI
    val host = getHost(uri, request.getHeaderValue("Host"))
    val userAgent = getUserAgent(request.getHeaderValue("User-Agent"))
    val headers = Header("Host", host) +: Header("User-Agent", userAgent) +:
      request.headers.filterNot(header => header.name.matches("(?i)Host|User-Agent"))

    val conn = getConnection(uri.withScheme(scheme).withAuthority(host))
    try handler(conn.send(request.withHeaders(headers : _*)))
    finally Try(conn.close())
  }

  private def getHost(uri: URI, default: => Option[String]): String =
    Option(uri.getAuthority).orElse(default).getOrElse(throw HeaderNotFound("Host"))

  private def getPort(uri: URI): Int =
    uri.getPort match {
      case -1   => if (uri.getScheme == "https") 443 else 80
      case port => port
    }

  private def getUserAgent(products: Option[String]): String =
    products.getOrElse(s"Java/${sys.props("java.version")} Scamper/0.12")

  private def getConnection(uri: URI): HttpClientConnection =
    HttpClientConnection(uri.getHost, getPort(uri), uri.getScheme == "https")
}
