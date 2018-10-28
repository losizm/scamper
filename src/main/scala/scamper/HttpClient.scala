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

import java.net.{ HttpURLConnection, URI, URL }
import java.time.{ LocalDate, LocalDateTime, OffsetDateTime }

import scala.annotation.tailrec
import scala.util.Try

import ImplicitExtensions._

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
    val headers = Header("Host", host) +: request.headers.filterNot(_.key.equalsIgnoreCase("Host"))

    uri.toURL(scheme, host).withConnection { implicit conn =>
      conn.setRequestMethod(request.method.name)
      request.headers.foreach(header => conn.addRequestProperty(header.key, header.value))

      if (!request.body.isKnownEmpty)
        writeBody(request.body)

      val response = HttpResponse(
        StatusLine.parse(conn.getHeaderField(0)),
        getResponseHeaders(),
        getResponseBody()
      )

      handler(response)
    }
  }

  private def getHost(uri: URI, default: => Option[String]): String =
    Option(uri.getAuthority).orElse(default).getOrElse(throw HeaderNotFound("Host"))

  private def writeBody(body: Entity)(implicit conn: HttpURLConnection): Unit = {
    conn.setDoOutput(true)

    body.length match {
      case Some(length) =>
        conn.setRequestProperty("Content-Length", length.toString)
        conn.setFixedLengthStreamingMode(length)

      case None =>
        conn.setRequestProperty("Transfer-Encoding", "chunked")
        conn.setChunkedStreamingMode(8192)
    }

    body.withInputStream { in =>
      val out = conn.getOutputStream
      val buf = new Array[Byte](8192)
      var len = 0

      while ({ len = in.read(buf); len != -1 })
        out.write(buf, 0, len)
    }
  }

  private def getResponseHeaders()(implicit conn: HttpURLConnection): Seq[Header] = {
    val headers = getResponseHeaders(1, Nil)

    if ("chunked".equalsIgnoreCase(conn.getHeaderField("Transfer-Encoding")))
      headers :+ Header.parse("X-Scamper-Transfer-Decoding: chunked")
    else headers
  }

  @tailrec
  private def getResponseHeaders(keyIndex: Int, headers: Seq[Header])(implicit conn: HttpURLConnection): Seq[Header] =
    conn.getHeaderFieldKey(keyIndex) match {
      case null => headers
      case key  => getResponseHeaders(keyIndex + 1, headers :+ Header(key, conn.getHeaderField(keyIndex)))
    }

  private def getResponseBody()(implicit conn: HttpURLConnection): Entity =
    Entity { () =>
      if (conn.getResponseCode < 400) conn.getInputStream
      else conn.getErrorStream
    }
}
