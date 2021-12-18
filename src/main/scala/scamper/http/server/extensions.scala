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
import java.net.Socket

import scamper.http.headers.{ Accept, ContentDisposition, ContentLength, ContentType, Expect }
import scamper.http.types.{ DispositionType, MediaRange, MediaType }
import scamper.logging.{ Logger, NullLogger }

import ResponseStatus.Registry.Continue

/** Adds server extensions to `HttpMessage`. */
implicit class ServerHttpMessage(message: HttpMessage) extends AnyVal:
  /**
   * Gets message correlate.
   *
   * Each incoming request is assigned a tag (i.e., correlate), which is later
   * reassigned to its outgoing response.
   */
  def correlate: String =
    message.getAttribute("scamper.http.server.message.correlate").get

  /** Gets message socket. */
  def socket: Socket =
    message.getAttribute("scamper.http.server.message.socket").get

  /**
   * Gets request count.
   *
   * The request count is the number of requests that have been received from
   * connection.
   */
  def requestCount: Int =
    message.getAttribute("scamper.http.server.message.requestCount").get

  /**
   * Gets server logger.
   *
   * @see [[HttpServer.logger HttpServer.logger()]]
   */
  def logger: Logger =
    message.getAttributeOrElse("scamper.http.server.message.logger", NullLogger)

  /** Gets server to which this message belongs. */
  def server: HttpServer =
    message.getAttribute("scamper.http.server.message.server").get

/** Adds server extensions to `HttpRequest`. */
implicit class ServerHttpRequest(request: HttpRequest) extends AnyVal:
  /** Gets path parameters. */
  def params: PathParameters =
    request.getAttributeOrElse("scamper.http.server.request.parameters", MapPathParameters(Map.empty))

  /**
   * Sends interim 100 (Continue) response if request includes Expect header
   * set to 100-Continue.
   *
   * @return `true` if response was sent; `false` otherwise
   */
  def continue(): Boolean =
    request.getExpect
      .collect { case value if value.toLowerCase == "100-continue" => request.socket }
      .map { socket =>
        socket.writeLine(StatusLine(Continue).toString)
        socket.writeLine()
        socket.flush()
      }.isDefined

  /**
   * Finds accepted media type among supplied media types.
   *
   * The matching media type with the highest weight is returned. If multiple
   * matches are found with equal weight, the first match is returned.
   */
  def findAccepted(types: Seq[MediaType]): Option[MediaType] =
    val ranges = request.accept match
      case Nil    => Seq(MediaRange("*/*"))
      case accept => accept.sortBy(_.weight * -1)

    types.flatMap { t => ranges.find(_.matches(t)).map(_.weight -> t) }
      .sortBy(_._1 * -1)
      .headOption
      .map(_._2)

/** Adds server extensions to `HttpResponse`. */
implicit class ServerHttpResponse(response: HttpResponse) extends AnyVal:
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
