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
package websocket

/** Provides standardized access to Sec-WebSocket-Accept header. */
given toSecWebSocketAccept: Conversion[HttpResponse, SecWebSocketAccept] = SecWebSocketAccept(_)

/** Provides standardized access to Sec-WebSocket-Accept header. */
class SecWebSocketAccept(response: HttpResponse) extends AnyVal:
  /** Tests for Sec-WebSocket-Accept header. */
  def hasSecWebSocketAccept: Boolean =
    response.hasHeader("Sec-WebSocket-Accept")

  /**
   * Gets Sec-WebSocket-Accept header value.
   *
   * @throws HeaderNotFound if Sec-WebSocket-Accept is not present
   */
  def secWebSocketAccept: String =
    secWebSocketAcceptOption.getOrElse(throw HeaderNotFound("Sec-WebSocket-Accept"))

  /** Gets Sec-WebSocket-Accept header value if present. */
  def secWebSocketAcceptOption: Option[String] =
    response.getHeaderValue("Sec-WebSocket-Accept")

  /** Creates new response with Sec-WebSocket-Accept header set to supplied value. */
  def setSecWebSocketAccept(value: String): HttpResponse =
    response.putHeaders(Header("Sec-WebSocket-Accept", value))

  /** Creates new response with Sec-WebSocket-Accept header removed. */
  def secWebSocketAcceptRemoved: HttpResponse =
    response.removeHeaders("Sec-WebSocket-Accept")
