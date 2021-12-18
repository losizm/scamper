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

/** Provides standardized access to Sec-WebSocket-Protocol-Client header. */
implicit class SecWebSocketProtocolClient(request: HttpRequest) extends AnyVal:
  /** Tests for Sec-WebSocket-Protocol-Client header. */
  def hasSecWebSocketProtocolClient: Boolean =
    request.hasHeader("Sec-WebSocket-Protocol-Client")

  /**
   * Gets Sec-WebSocket-Protocol-Client header value.
   *
   * @throws HeaderNotFound if Sec-WebSocket-Protocol-Client is not present
   */
  def secWebSocketProtocolClient: String =
    getSecWebSocketProtocolClient.getOrElse(throw HeaderNotFound("Sec-WebSocket-Protocol-Client"))

  /** Gets Sec-WebSocket-Protocol-Client header value if present. */
  def getSecWebSocketProtocolClient: Option[String] =
    request.getHeaderValue("Sec-WebSocket-Protocol-Client")

  /** Creates new request with Sec-WebSocket-Protocol-Client header set to supplied value. */
  def setSecWebSocketProtocolClient(value: String): HttpRequest =
    request.putHeaders(Header("Sec-WebSocket-Protocol-Client", value))

  /** Creates new request with Sec-WebSocket-Protocol-Client header removed. */
  def removeSecWebSocketProtocolClient: HttpRequest =
    request.removeHeaders("Sec-WebSocket-Protocol-Client")
