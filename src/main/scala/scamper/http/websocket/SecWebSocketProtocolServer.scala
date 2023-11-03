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

/** Adds standardized access to Sec-WebSocket-Protocol-Client header. */
given toSecWebSocketProtocolServer: Conversion[HttpResponse, SecWebSocketProtocolServer] = SecWebSocketProtocolServer(_)

/** Adds standardized access to Sec-WebSocket-Protocol-Server header. */
class SecWebSocketProtocolServer(response: HttpResponse) extends AnyVal:
  /** Tests for Sec-WebSocket-Protocol-Server header. */
  def hasSecWebSocketProtocolServer: Boolean =
    response.hasHeader("Sec-WebSocket-Protocol-Server")

  /**
   * Gets Sec-WebSocket-Protocol-Server header value.
   *
   * @throws HeaderNotFound if Sec-WebSocket-Protocol-Server is not present
   */
  def secWebSocketProtocolServer: String =
    secWebSocketProtocolServerOption.getOrElse(throw HeaderNotFound("Sec-WebSocket-Protocol-Server"))

  /** Gets Sec-WebSocket-Protocol-Server header value if present. */
  def secWebSocketProtocolServerOption: Option[String] =
    response.getHeaderValue("Sec-WebSocket-Protocol-Server")

  /** Creates new response with Sec-WebSocket-Protocol-Server header set to supplied value. */
  def setSecWebSocketProtocolServer(value: String): HttpResponse =
    response.putHeaders(Header("Sec-WebSocket-Protocol-Server", value))

  /** Creates new response with Sec-WebSocket-Protocol-Server header removed. */
  def secWebSocketProtocolServerRemoved: HttpResponse =
    response.removeHeaders("Sec-WebSocket-Protocol-Server")
