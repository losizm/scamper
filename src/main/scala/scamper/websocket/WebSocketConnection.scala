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

import scala.util.Try

import java.io.{ DataInputStream, DataOutputStream, EOFException }
import java.net.Socket

import javax.net.ssl.SSLSocket

import StatusCode.Registry._

/**
 * Represents endpoint of websocket connection.
 *
 * @see [[WebSocketConnection.apply]]
 */
class WebSocketConnection private (socket: Socket) {
  private val finBits      = 0x80
  private val reservedBits = 0x70
  private val opcodeBits   = 0x0f
  private val maskBits     = 0x80
  private val lengthBits   = 0x7f

  private val in = new DataInputStream(socket.getInputStream())
  private val out = new DataOutputStream(socket.getOutputStream())

  /** Tests whether websocket connection is using secure transport. */
  def isSecure: Boolean = socket.isInstanceOf[SSLSocket]

  /** Tests whether websocket connection is open. */
  def isOpen: Boolean = !socket.isClosed()

  /**
   * Reads next frame from websocket connection.
   *
   * @param timeout maximum time to wait for any read operation; `0` is
   *  interpreted as indefinitely
   *
   * @throws WebSocketError if frame is invalid or otherwise unreadable
   * @throws EOFException if unexpected EOF occurs
   * @throws InterruptedIOException if timeout is exceeded
   *
   * @note This method is synchronized on the websocket's input channel,
   *  permitting only one read request at a time.
   */
  def read(timeout: Int = 0): WebSocketFrame = in.synchronized {
    socket.setSoTimeout(timeout)

    val byte0 = in.readUnsignedByte()
    val isFinal = (byte0 & finBits) != 0

    if ((byte0 & reservedBits) != 0)
      throw WebSocketError(ProtocolError)

    val opcode = Opcode.get(byte0 & opcodeBits).getOrElse {
      throw WebSocketError(ProtocolError)
    }

    if (opcode.isControl && !isFinal)
      throw WebSocketError(ProtocolError)

    val byte1 = in.readUnsignedByte()
    val isMasked = (byte1 & maskBits) != 0

    val length = (byte1 & lengthBits) match {
      case 126 => in.readUnsignedShort()

      case 127 =>
        val length = in.readLong()

        // If length is negative, then it represents an unsigned long value
        // greater than Long.MaxValue
        if (length < 0)
          throw WebSocketError(MessageTooBig)
        length

      case length => length
    }

    val maskingKey = in.readInt() match {
      case 0 => None
      case n => Some(n)
    }

    if (isMasked ^ maskingKey.isDefined)
      throw WebSocketError(ProtocolError)

    WebSocketFrame(isFinal, opcode, maskingKey, length, in)
  }

  /**
   * Writes supplied frame to websocket connection.
   *
   * @param frame websocket frame
   *
   * @throws EOFException if payload truncation is detected
   *
   * @note This method is synchronized on the websocket's output channel,
   *  permitting only one write request at a time.
   */
  def write(frame: WebSocketFrame): Unit = out.synchronized {
    val finBit = frame.isFinal match {
      case true  => -128
      case false => 0
    }

    val maskBit = frame.maskingKey.isDefined match {
      case true  => -128
      case false => 0
    }

    out.write(finBit + frame.opcode.value)

    frame.length match {
      case length if length <= 125   =>
        out.write(maskBit + length.toInt)
      case length if length <= 65536 =>
        out.write(maskBit + 126)
        out.writeShort(length.toInt)
      case length =>
        out.write(maskBit + 127)
        out.writeLong(length)
    }

    val buf = new Array[Byte](8192)
    val in = frame.payload
    var tot = 0
    var len = 0

    while ({ len = in.read(buf); len != -1 }) {
      out.write(buf, 0, len)
      tot += len
    }

    out.flush()

    if (tot < frame.length)
      throw new EOFException(s"Truncation dectected: Payload length ($tot) is less than declared length (${frame.length})")
  }

  /**
   * Closes websocket connection.
   *
   * @note This method does NOT send a Close frame. The user is responsible for
   *  sending an appropriate Close frame before invoking this method.
   */
  def close(): Unit = Try(socket.close())
}

/** Provides factory methods for `WebSocketConnection`. */
object WebSocketConnection {
  /**
   * Creates WebSocketConnection using supplied socket.
   *
   * @param socket socket connection
   */
  def apply(socket: Socket): WebSocketConnection = new WebSocketConnection(socket)
}
