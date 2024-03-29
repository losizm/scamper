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

import java.net.Socket

/** Adds server extensions to `HttpMessage`. */
given toServerHttpMessage: Conversion[HttpMessage, ServerHttpMessage] = ServerHttpMessage(_)

/** Adds server extensions to `HttpMessage`. */
class ServerHttpMessage(message: HttpMessage) extends AnyVal:
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

  /** Gets server to which this message belongs. */
  def server: HttpServer =
    message.getAttribute("scamper.http.server.message.server").get
