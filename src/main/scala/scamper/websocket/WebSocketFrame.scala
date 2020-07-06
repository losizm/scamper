/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.io.{ ByteArrayInputStream, InputStream }

import scamper.{ BoundedInputStream, EmptyInputStream }
import Opcode.Registry._

/** Defines frame for WebSocket message. */
trait WebSocketFrame {
  /** Tests for message final frame. */
  def isFinal: Boolean

  /** Gets opcode. */
  def opcode: Opcode

  /** Gets masking key. */
  def key: Option[MaskingKey]

  /** Gets payload length. */
  def length: Long

  /** Gets input stream to payload. */
  def payload: InputStream

  /** Tests for presence of masking key. */
  def isMasked: Boolean = key.isDefined

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
   * @param isFinal indicates final frame of message
   * @param opcode frame opcode
   * @param key masking key
   * @param length payload length
   * @param payload input stream to payload
   */
  def apply(isFinal: Boolean, opcode: Opcode, key: Option[MaskingKey], length: Long, payload: InputStream): WebSocketFrame = {
    if (opcode.isControl) {
      if (!isFinal)
        throw new IllegalArgumentException("isFinal not set for control frame")

      if (length > 125)
        throw new IllegalArgumentException("length greater than 125 bytes for control frame")
    }

    if (key == null)
      throw new NullPointerException("key")

    key.foreach { value =>
      if (value == null)
        throw new NullPointerException("key")
    }

    if (length < 0)
      throw new IllegalArgumentException("length less than zero")

    if (payload == null)
      throw new NullPointerException("payload")

    new WebSocketFrameImpl(isFinal, opcode, key, length, new BoundedInputStream(payload, length))
  }

  /**
   * Creates WebSocketFrame using supplied attributes.
   *
   * @param isFinal indicates final frame of message
   * @param opcode frame opcode
   * @param key masking key
   * @param length payload length
   * @param data unmasked payload data
   *
   * @note If there is `Some` masking key, it is used to mask `data`.
   */
  def apply(isFinal: Boolean, opcode: Opcode, key: Option[MaskingKey], length: Int, data: Array[Byte]): WebSocketFrame = {
    key.foreach(key => key(data, length, 0))
    apply(isFinal, opcode, key, length, { if (length == 0) EmptyInputStream else new ByteArrayInputStream(data, 0, length) })
  }

  /**
   * Creates WebSocketFrame using supplied attributes.
   *
   * @param isFinal indicates final frame of message
   * @param opcode frame opcode
   * @param key masking key
   * @param data unmasked payload data
   *
   * @note If there is `Some` masking key, it is used to mask `data`.
   */
  def apply(isFinal: Boolean, opcode: Opcode, key: Option[MaskingKey], data: Array[Byte]): WebSocketFrame =
    apply(isFinal, opcode, key, data.size, data)

  /**
   * Creates Close frame using supplied status code.
   *
   * @param statusCode status code to serve as payload
   * @param key masking key
   *
   * @note If there is `Some` masking key, it is used to mask status code.
   */
  def apply(statusCode: StatusCode, key: Option[MaskingKey]): WebSocketFrame =
    apply(true, Close, key, 2, statusCode.toData)
}

private case class WebSocketFrameImpl(
  isFinal: Boolean,
  opcode: Opcode,
  key: Option[MaskingKey],
  length: Long,
  payload: InputStream
) extends WebSocketFrame
