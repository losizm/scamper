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
package scamper.server

import java.io.File
import java.net.Socket

import scamper.{ Auxiliary, ContentEncoder, Entity, HttpMessage, HttpRequest, HttpResponse, StatusLine }
import scamper.ResponseStatus.Registry.Continue
import scamper.headers.{ ContentDisposition, ContentLength, ContentType, Expect }
import scamper.logging.{ Logger, NullLogger }
import scamper.types.{ DispositionType, MediaType }

import Auxiliary.{ SocketType, StringType }

/** Defines server-side implicit classes. */
object Implicits:
  /** Adds server-side extension methods to `HttpMessage`. */
  implicit class ServerHttpMessage(msg: HttpMessage) extends AnyVal:
    /**
     * Gets message correlate.
     *
     * Each incoming request is assigned a tag (i.e., correlate), which is later
     * reassigned to its outgoing response.
     */
    def correlate: String =
      msg.getAttribute("scamper.server.message.correlate").get

    /** Gets message socket. */
    def socket: Socket =
      msg.getAttribute("scamper.server.message.socket").get

    /**
     * Gets request count.
     *
     * The request count is the number of requests that have been received from
     * connection.
     */
    def requestCount: Int =
      msg.getAttribute("scamper.server.message.requestCount").get

    /**
     * Gets server logger.
     *
     * @see [[HttpServer.logger HttpServer.logger()]]
     */
    def logger: Logger =
      msg.getAttributeOrElse("scamper.server.message.logger", NullLogger)

    /** Gets server to which this message belongs. */
    def server: HttpServer =
      msg.getAttribute("scamper.server.message.server").get

  /** Adds server-side extension methods to `HttpRequest`. */
  implicit class ServerHttpRequest(req: HttpRequest) extends AnyVal:
    /** Gets path parameters. */
    def params: PathParameters =
      req.getAttributeOrElse("scamper.server.request.parameters", MapPathParameters(Map.empty))

    /**
     * Sends interim 100 (Continue) response if request includes Expect header
     * set to 100-Continue.
     *
     * @return `true` if response was sent; `false` otherwise
     */
    def continue(): Boolean =
      req.getExpect
        .collect { case value if value.toLowerCase == "100-continue" => req.socket }
        .map { socket =>
          socket.writeLine(StatusLine(Continue).toString)
          socket.writeLine()
          socket.flush()
        }.isDefined

  /** Adds server-side extension methods to `HttpResponse`. */
  implicit class ServerHttpResponse(res: HttpResponse) extends AnyVal:
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
      res.getAttribute("scamper.server.response.request")

    /**
     * Adds `gzip` to Content-Encoding header and encodes message body.
     *
     * @param bufferSize size in bytes of buffer used to encode message body
     *
     * @return new response
     */
    def setGzipContentEncoding(bufferSize: Int = 8192): HttpResponse =
      ContentEncoder.gzip(res, bufferSize)(using Auxiliary.executor)

    /**
     * Adds `deflate` to Content-Encoding header and encodes message body.
     *
     * @param bufferSize size in bytes of buffer used to encode message body
     *
     * @return new response
     */
    def setDeflateContentEncoding(bufferSize: Int = 8192): HttpResponse =
      ContentEncoder.deflate(res, bufferSize)

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
      val mediaType = MediaType.forFile(file).getOrElse(Auxiliary.applicationOctetStream)
      val disposition = DispositionType(
        typeName,
        "filename" -> file.getName(),
        "filename*" -> s"utf-8''${file.getName().toUrlEncoded("utf-8")}"
      )

      res.setBody(entity)
        .setContentType(mediaType)
        .setContentLength(entity.knownSize.get)
        .setContentDisposition(disposition)
