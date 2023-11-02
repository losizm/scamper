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

/** Provides standardized access to Sec-WebSocket-Version header. */
given toSecWebSocketVersion[T <: HttpMessage]: Conversion[T, SecWebSocketVersion[T]] = SecWebSocketVersion(_)

/** Provides standardized access to Sec-WebSocket-Version header. */
class SecWebSocketVersion[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Sec-WebSocket-Version header. */
  def hasSecWebSocketVersion: Boolean =
    message.hasHeader("Sec-WebSocket-Version")

  /**
   * Gets Sec-WebSocket-Version header value.
   *
   * @throws HeaderNotFound if Sec-WebSocket-Version is not present
   */
  def secWebSocketVersion: String =
    secWebSocketVersionOption.getOrElse(throw HeaderNotFound("Sec-WebSocket-Version"))

  /** Gets Sec-WebSocket-Version header value if present. */
  def secWebSocketVersionOption: Option[String] =
    message.getHeaderValue("Sec-WebSocket-Version")

  /** Creates new message with Sec-WebSocket-Version header set to supplied value. */
  def setSecWebSocketVersion(value: String): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Sec-WebSocket-Version", value))

  /** Creates new message with Sec-WebSocket-Version header removed. */
  def secWebSocketVersionRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Sec-WebSocket-Version")
