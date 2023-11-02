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
package client

/** Adds client extensions to `HttpRequest`. */
given toClientHttpRequest: Conversion[HttpRequest, ClientHttpRequest] = ClientHttpRequest(_)

/** Adds client extensions to `HttpRequest`. */
class ClientHttpRequest(request: HttpRequest) extends AnyVal:
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
