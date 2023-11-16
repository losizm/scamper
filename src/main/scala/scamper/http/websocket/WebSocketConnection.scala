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

import scala.util.Try

import java.io.{ DataInputStream, DataOutputStream, EOFException }
import java.net.Socket

import javax.net.ssl.SSLSocket

import Opcode.Registry.{ Binary, Continuation, Text }
import StatusCode.Registry.{ MessageTooBig, ProtocolError }

/**
 * Represents endpoint of WebSocket connection.
 *
 * @constructor Create WebSocket connection using supplied socket.
 */
class WebSocketConnection(socket: Socket) extends AutoCloseable:
  private val finBits      = 0x80
  private val compressBits = 0x40
  private val reservedBits = 0x30
  private val opcodeBits   = 0x0f
  private val maskBits     = 0x80
  private val lengthBits   = 0x7f

  private val in = DataInputStream(socket.getInputStream())
  private val out = DataOutputStream(socket.getOutputStream())

  /** Tests for secure WebSocket connection. */
  def isSecure: Boolean = socket.isInstanceOf[SSLSocket]

  /** Tests whether WebSocket connection is open. */
  def isOpen: Boolean = socket.isConnected && !socket.isClosed

  /**
   * Reads next frame from WebSocket connection.
   *
   * @param timeout timeout in milliseconds of any read operation; `0` is
   * interpreted as indefinite
   *
   * @throws WebSocketError if frame is invalid or otherwise unreadable
   * @throws java.io.EOFException if unexpected EOF occurs
   * @throws java.io.InterruptedIOException if timeout is exceeded
   *
   * @note This method is synchronized on the WebSocket's input channel,
   *  permitting only one read request at a time.
   */
  def read(timeout: Int = 0): WebSocketFrame = in.synchronized {
    socket.setSoTimeout(timeout)

    val byte0 = in.readUnsignedByte()
    val isFinal = (byte0 & finBits) != 0
    val isCompressed = (byte0 & compressBits) != 0

    if (byte0 & reservedBits) != 0 then
      throw WebSocketError(ProtocolError)

    val opcode = Opcode.get(byte0 & opcodeBits).getOrElse {
      throw WebSocketError(ProtocolError)
    }

    if isCompressed && opcode != Text && opcode != Binary && opcode != Continuation then
      throw WebSocketError(ProtocolError)

    if opcode.isControl && !isFinal then
      throw WebSocketError(ProtocolError)

    val byte1 = in.readUnsignedByte()
    val isMasked = (byte1 & maskBits) != 0

    val length: Long = (byte1 & lengthBits) match
      case 126 => in.readUnsignedShort()

      case 127 =>
        val length = in.readLong()

        // If length is negative, then it represents an unsigned long value
        // greater than Long.MaxValue
        if length < 0 then
          throw WebSocketError(MessageTooBig)
        length

      case length => length

    val key = isMasked match
      case true  => MaskingKey.get(in.readInt())
      case false => None

    if isMasked ^ key.isDefined then
      throw WebSocketError(ProtocolError)

    WebSocketFrame(isFinal, isCompressed, opcode, key, length, in)
  }

  /**
   * Writes supplied frame to WebSocket connection.
   *
   * @param frame WebSocket frame
   *
   * @throws java.io.EOFException if payload truncation is detected
   *
   * @note This method is synchronized on the WebSocket's output channel,
   *  permitting only one write request at a time.
   */
  def write(frame: WebSocketFrame): Unit = out.synchronized {
    val finBit = frame.isFinal match
      case true  => 128
      case false => 0

    val compressBit = frame.isCompressed match
      case true  => 64
      case false => 0

    out.write(finBit + compressBit + frame.opcode.value)

    val maskBit = frame.key.isDefined match
      case true  => 128
      case false => 0

    frame.length match
      case length if length <= 125 =>
        out.write(maskBit + length.toInt)

      case length if length <= 65535 =>
        out.write(maskBit + 126)
        out.writeShort(length.toInt)

      case length =>
        out.write(maskBit + 127)
        out.writeLong(length)

    frame.key.foreach { key =>
      out.writeInt(key.value)
    }

    if frame.length > 0 then
      val length = out.write(frame.payload, new Array[Byte](8192))

      if length < frame.length then
        throw EOFException(s"Truncation dectected: Payload length ($length) is less than declared length (${frame.length})")

    out.flush()
  }

  /**
   * Closes WebSocket connection.
   *
   * @note This method does NOT send a Close frame. The user is responsible for
   *  sending an appropriate Close frame before invoking this method.
   */
  def close(): Unit = Try(socket.close())
