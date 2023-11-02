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

/** Provides standardized access to Sec-WebSocket-Protocol header. */
given toSecWebSocketProtocol[T <: HttpMessage]: Conversion[T, SecWebSocketProtocol[T]] = SecWebSocketProtocol(_)

/** Provides standardized access to Sec-WebSocket-Protocol header. */
class SecWebSocketProtocol[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Sec-WebSocket-Protocol header. */
  def hasSecWebSocketProtocol: Boolean =
    message.hasHeader("Sec-WebSocket-Protocol")

  /**
   * Gets Sec-WebSocket-Protocol header values.
   *
   * @throws HeaderNotFound if Sec-WebSocket-Protocol is not present
   */
  def secWebSocketProtocol: Seq[String] =
    secWebSocketProtocolOption.getOrElse(Nil)

  /** Gets Sec-WebSocket-Protocol header values if present. */
  def secWebSocketProtocolOption: Option[Seq[String]] =
    message.getHeaderValue("Sec-WebSocket-Protocol").map(ListParser.apply)

  /** Creates new message with Sec-WebSocket-Protocol header set to supplied values. */
  def setSecWebSocketProtocol(values: Seq[String]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Sec-WebSocket-Protocol", values.mkString(", ")))

  /** Creates new message with Sec-WebSocket-Protocol header set to supplied values. */
  def setSecWebSocketProtocol(one: String, more: String*): T =
    setSecWebSocketProtocol(one +: more)

  /** Creates new message with Sec-WebSocket-Protocol header removed. */
  def secWebSocketProtocolRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Sec-WebSocket-Protocol")
