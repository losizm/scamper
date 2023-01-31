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

/** Provides standardized access to Sec-WebSocket-Key header. */
implicit class SecWebSocketKey(request: HttpRequest) extends AnyVal:
  /** Tests for Sec-WebSocket-Key header. */
  def hasSecWebSocketKey: Boolean =
    request.hasHeader("Sec-WebSocket-Key")

  /**
   * Gets Sec-WebSocket-Key header value.
   *
   * @throws HeaderNotFound if Sec-WebSocket-Key is not present
   */
  def secWebSocketKey: String =
    secWebSocketKeyOption.getOrElse(throw HeaderNotFound("Sec-WebSocket-Key"))

  /** Gets Sec-WebSocket-Key header value if present. */
  def secWebSocketKeyOption: Option[String] =
    request.getHeaderValue("Sec-WebSocket-Key")

  /** Creates new request with Sec-WebSocket-Key header set to supplied value. */
  def setSecWebSocketKey(value: String): HttpRequest =
    request.putHeaders(Header("Sec-WebSocket-Key", value))

  /** Creates new request with Sec-WebSocket-Key header removed. */
  def secWebSocketKeyRemoved: HttpRequest =
    request.removeHeaders("Sec-WebSocket-Key")
