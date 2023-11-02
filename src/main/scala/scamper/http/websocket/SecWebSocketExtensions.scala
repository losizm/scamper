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

/** Provides standardized access to Sec-WebSocket-Extensions header. */
given toSecWebSocketExtensions[T <: HttpMessage]: Conversion[T, SecWebSocketExtensions[T]] = SecWebSocketExtensions(_)

/** Provides standardized access to Sec-WebSocket-Extensions header. */
class SecWebSocketExtensions[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Sec-WebSocket-Extensions header. */
  def hasSecWebSocketExtensions: Boolean =
    message.hasHeader("Sec-WebSocket-Extensions")

  /**
   * Gets Sec-WebSocket-Extensions header values.
   *
   * @throws HeaderNotFound if Sec-WebSocket-Extensions is not present
   */
  def secWebSocketExtensions: Seq[WebSocketExtension] =
    secWebSocketExtensionsOption.getOrElse(Nil)

  /** Gets Sec-WebSocket-Extensions header values if present. */
  def secWebSocketExtensionsOption: Option[Seq[WebSocketExtension]] =
    message.getHeaderValues("Sec-WebSocket-Extensions")
      .map(WebSocketExtension.parseAll)
      .reduceLeftOption(_ ++ _)

  /** Creates new message with Sec-WebSocket-Extensions header set to supplied values. */
  def setSecWebSocketExtensions(values: Seq[WebSocketExtension]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Sec-WebSocket-Extensions", values.mkString(", ")))

  /** Creates new message with Sec-WebSocket-Extensions header set to supplied values. */
  def setSecWebSocketExtensions(one: WebSocketExtension, more: WebSocketExtension*): T =
    setSecWebSocketExtensions(one +: more)

  /** Creates new message with Sec-WebSocket-Extensions header removed. */
  def secWebSocketExtensionsRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Sec-WebSocket-Extensions")
