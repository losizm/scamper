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
package client

import java.net.Socket

/** Adds client extensions to `HttpMessage`. */
implicit class ClientHttpMessage(message: HttpMessage) extends AnyVal:
  /** Gets message socket. */
  def socket: Socket = message.getAttribute("scamper.client.message.socket").get

  /**
   * Gets message correlate.
   *
   * Each outgoing request is assigned a tag (i.e., correlate), which is later
   * reassigned to its incoming response.
   */
  def correlate: String = message.getAttribute("scamper.client.message.correlate").get

  /**
   * Gets absolute target.
   *
   * The absolute target (i.e., absolute URI) is assigned to each outgoing
   * request and later reassigned to its incoming response.
   */
  def absoluteTarget: Uri = message.getAttribute("scamper.client.message.absoluteTarget").get

  /** Gets client to which this message belongs. */
  def client: HttpClient = message.getAttribute("scamper.client.message.client").get

/** Adds client extensions to `HttpRequest`. */
implicit class ClientHttpRequest(request: HttpRequest) extends AnyVal:
  /**
   * Sends request and passes response to given handler.
   *
   * @param handler response handler
   *
   * @see [[HttpClient!.send HttpClient.send()]]
   *
   * @note To make effective use of this method, `request.target` must be an
   *   absolute URI.
   */
  def send[T](handler: ResponseHandler[T])(using client: HttpClient): T =
    client.send(request)(handler)

  /**
   * Adds `gzip` to Content-Encoding header and encodes message body.
   *
   * @param bufferSize size in bytes of buffer used to encode message body
   *
   * @return new request
   */
  def setGzipContentEncoding(bufferSize: Int = 8192): HttpRequest =
    ContentEncoder.gzip(request, bufferSize)(using Auxiliary.executor)

  /**
   * Adds `deflate` to Content-Encoding header and encodes message body.
   *
   * @param bufferSize size in bytes of buffer used to encode message body
   *
   * @return new request
   */
  def setDeflateContentEncoding(bufferSize: Int = 8192): HttpRequest =
    ContentEncoder.deflate(request, bufferSize)

/** Adds client extensions to `HttpResponse`. */
implicit class ClientHttpResponse(response: HttpResponse) extends AnyVal:
  /**
   * Gets corresponding request.
   *
   * @note The request is the outgoing request after filters are applied, and
   * the message entity's input stream is an active object.
   */
  def request: HttpRequest =
    response.getAttribute("scamper.client.response.request").get
