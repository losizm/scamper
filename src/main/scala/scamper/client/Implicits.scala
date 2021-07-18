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
package scamper.client

import java.net.Socket

import scamper.*

/** Defines client-side implicit classes. */
object Implicits:
  /** Adds client-side extension methods to `HttpMessage`. */
  implicit class ClientHttpMessage(msg: HttpMessage) extends AnyVal:
    /** Gets message socket. */
    def socket: Socket = msg.getAttribute("scamper.client.message.socket").get

    /**
     * Gets message correlate.
     *
     * Each outgoing request is assigned a tag (i.e., correlate), which is later
     * reassigned to its incoming response.
     */
    def correlate: String = msg.getAttribute("scamper.client.message.correlate").get

    /**
     * Gets absolute target.
     *
     * The absolute target (i.e., absolute URI) is assigned to each outgoing
     * request and later reassigned to its incoming response.
     */
    def absoluteTarget: Uri = msg.getAttribute("scamper.client.message.absoluteTarget").get

    /** Gets client to which this message belongs. */
    def client: HttpClient = msg.getAttribute("scamper.client.message.client").get

  /** Adds client-side extension methods to `HttpRequest`. */
  implicit class ClientHttpRequest(req: HttpRequest) extends AnyVal:
    /**
     * Sends request and passes response to given handler.
     *
     * @param handler response handler
     *
     * @see [[HttpClient!.send HttpClient.send()]]
     *
     * @note To make effective use of this method, `req.target` must be an
     *   absolute URI.
     */
    def send[T](handler: ResponseHandler[T])(using client: HttpClient): T =
      client.send(req)(handler)

    /**
     * Adds `gzip` to Content-Encoding header and encodes message body.
     *
     * @param bufferSize size in bytes of buffer used to encode message body
     *
     * @return new request
     */
    def setGzipContentEncoding(bufferSize: Int = 8192): HttpRequest =
      ContentEncoder.gzip(req, bufferSize)(using Auxiliary.executor)

    /**
     * Adds `deflate` to Content-Encoding header and encodes message body.
     *
     * @param bufferSize size in bytes of buffer used to encode message body
     *
     * @return new request
     */
    def setDeflateContentEncoding(bufferSize: Int = 8192): HttpRequest =
      ContentEncoder.deflate(req, bufferSize)

  /** Adds client-side extension methods to `HttpResponse`. */
  implicit class ClientHttpResponse(res: HttpResponse) extends AnyVal:
    /**
     * Gets corresponding request.
     *
     * @note The request is the outgoing request after filters are applied, and
     * the message entity's input stream is an active object.
     */
    def request: HttpRequest =
      res.getAttribute("scamper.client.response.request").get
