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
package server

import java.io.File

import scamper.http.headers.{ toContentDisposition, toContentLength, toContentType }
import scamper.http.types.{ DispositionType, MediaType }

/** Adds server extensions to `HttpResponse`. */
given toServerHttpResponse: Conversion[HttpResponse, ServerHttpResponse] = ServerHttpResponse(_)

/** Adds server extensions to `HttpResponse`. */
class ServerHttpResponse(response: HttpResponse) extends AnyVal:
  /**
   * Optionally gets corresponding request.
   *
   * The request is not available if server could not read it or the server
   * rejected it before it was read.
   *
   * @note The request is the incoming request before handlers are applied;
   * however, the message entity's input stream is an active object.
   */
  def request: Option[HttpRequest] =
    response.getAttribute("scamper.http.server.response.request")

  /**
   * Adds `gzip` to Content-Encoding header and encodes message body.
   *
   * @param bufferSize size in bytes of buffer used to encode message body
   *
   * @return new response
   */
  def setGzipContentEncoding(bufferSize: Int = 8192): HttpResponse =
    ContentEncoder.gzip(response, bufferSize)(using Auxiliary.executor)

  /**
   * Adds `deflate` to Content-Encoding header and encodes message body.
   *
   * @param bufferSize size in bytes of buffer used to encode message body
   *
   * @return new response
   */
  def setDeflateContentEncoding(bufferSize: Int = 8192): HttpResponse =
    ContentEncoder.deflate(response, bufferSize)

  /**
   * Creates new response with supplied file as attachment.
   *
   * The Content-Type, Content-Length, and Content-Disposition headers are set
   * accordingly.
   *
   * @param file attachment
   *
   * @return new response
   */
  def setAttachment(file: File): HttpResponse =
    createWithContentDisposition("attachment", file)

  /**
   * Creates new response with supplied file as inline content.
   *
   * The Content-Type, Content-Length, and Content-Disposition headers are set
   * accordingly.
   *
   * @param file inline content
   *
   * @return new response
   */
  def setInline(file: File): HttpResponse =
    createWithContentDisposition("inline", file)

  private def createWithContentDisposition(typeName: String, file: File): HttpResponse =
    import scala.language.implicitConversions

    val entity = Entity(file)
    val mediaType = MediaType.forFile(file).getOrElse(MediaType.octetStream)
    val disposition = DispositionType(
      typeName,
      "filename" -> file.getName(),
      "filename*" -> s"utf-8''${file.getName().toUrlEncoded}"
    )

    response.setBody(entity)
      .setContentType(mediaType)
      .setContentLength(entity.knownSize.get)
      .setContentDisposition(disposition)
