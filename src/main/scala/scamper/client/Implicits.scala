/*
 * Copyright 2019 Carlos Conyers
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

import scamper.{ Auxiliary, ContentEncoder, HttpRequest }

/** Includes client-side type classes. */
object Implicits {
  /** Adds client-side extension methods to `HttpRequest`. */
  implicit class ClientHttpRequestType(val req: HttpRequest) extends AnyVal {
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
    def send[T](handler: ResponseHandler[T])(implicit client: HttpClient): T =
      client.send(req)(handler)

    /**
     * Adds `gzip` to `Content-Encoding` header and encodes message body.
     *
     * @param bufferSize size in bytes of buffer used to encode message body
     *
     * @return new request
     */
    def withGzipContentEncoding(bufferSize: Int = 8192): HttpRequest =
      ContentEncoder.gzip(req, bufferSize)(Auxiliary.executor)

    /**
     * Adds `deflate` to `Content-Encoding` header and encodes message body.
     *
     * @param bufferSize size in bytes of buffer used to encode message body
     *
     * @return new request
     */
    def withDeflateContentEncoding(bufferSize: Int = 8192): HttpRequest =
      ContentEncoder.deflate(req, bufferSize)(Auxiliary.executor)
  }
}
