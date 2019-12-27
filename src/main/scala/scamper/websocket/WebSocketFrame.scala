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
package scamper.websocket

import java.io.InputStream

import scamper.BoundedInputStream
import Opcode.Registry._

/** Defines frame for WebSocket message. */
trait WebSocketFrame {
  /** Tests for message final frame. */
  def isFinal: Boolean

  /** Gets opcode. */
  def opcode: Opcode

  /** Gets payload masking key. */
  def maskingKey: Option[Int]

  /** Gets payload length. */
  def length: Long

  /** Gets input stream to payload data. */
  def payload: InputStream

  /** Tests for Continuation frame. */
  def isContinuation: Boolean = opcode == Continuation

  /** Tests for Text frame. */
  def isText: Boolean = opcode == Text

  /** Tests for Binary frame. */
  def isBinary: Boolean = opcode == Binary

  /** Tests for Close frame. */
  def isClose: Boolean = opcode == Close

  /** Tests for Ping frame. */
  def isPing: Boolean = opcode == Ping

  /** Tests for Pong frame. */
  def isPong: Boolean = opcode == Pong
}

/** Provides factory methods for `WebSocketFrame`. */
object WebSocketFrame {
  /**
   * Creates WebSocketFrame using supplied attributes.
   *
   * @param isFinal indicates whether supplied frame is message final frame
   * @param opcode frame opcode
   * @param maskingKey payload masking key
   * @param length payload length
   * @param payload input stream to payload data
   */
  def apply(isFinal: Boolean, opcode: Opcode, maskingKey: Option[Int], length: Long, payload: InputStream): WebSocketFrame = {
    if (opcode.isControl && !isFinal)
      throw new IllegalArgumentException("control frame must be final")

    if (maskingKey == null)
      throw new NullPointerException()

    if (length < 0)
      throw new IllegalArgumentException("length must be nonnegative")

    if (payload == null)
      throw new NullPointerException()

    new WebSocketFrameImpl(isFinal, opcode, maskingKey, length, new BoundedInputStream(payload, length))
  }
}

private case class WebSocketFrameImpl(
  isFinal: Boolean,
  opcode: Opcode,
  maskingKey: Option[Int],
  length: Long,
  payload: InputStream
) extends WebSocketFrame
