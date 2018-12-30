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
package scamper.client

import scamper.HttpRequest

/** Includes client-related type classes. */
object Implicits {
  /** Adds extension methods to `HttpRequest`. */
  implicit class ClientHttpRequestType(val req: HttpRequest) extends AnyVal {
    /**
     * Sends request and passes response to given handler.
     *
     * <strong>Note:</strong> To make effective use of this method, the request
     * target must be defined with an absolute URI.
     *
     * @param handler response handler
     */
    def send[T](handler: ResponseHandler[T])(implicit client: HttpClient): T = {
      require(req.target.isAbsolute, "Request target not absolute")
      client.send(req, req.target.getScheme == "https")(handler)
    }
  }
}
