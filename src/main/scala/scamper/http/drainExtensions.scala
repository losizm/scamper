/*
 * Copyright 2022 Carlos Conyers
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

import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.{ Channels, WritableByteChannel }

extension [T <: HttpMessage](message: T)
  /**
   * Drains decoded message body.
   *
   * @param maxLength maximum number of bytes
   *
   * @throws ReadLimitExceeded if body exceeds `maxLength`
   */
  def drain(maxLength: Long = 8388608): T =
    BodyDecoder(maxLength).withDecode(message) { in =>
      val buffer = new Array[Byte](8192)
      while in.read(buffer) != -1 do ()
      message
    }

  /**
   * Drains decoded message body to supplied sink.
   *
   * @param sink buffer to which message body is written
   *
   * @return number of bytes written to sink
   *
   * @throws BufferOverflowException &nbsp; if sink not large enough to hold
   * decoded message body
   */
  def drain(sink: Array[Byte]): Int =
    drain(ByteBuffer.wrap(sink)).position()

  /**
   * Drains decoded message body to supplied sink.
   *
   * @param sink   buffer to which message body is written
   * @param offset buffer position
   * @param length buffer size (starting at offset)
   *
   * @return number of bytes written to sink
   *
   * @throws IndexOutOfBoundsException &nbsp; if offset or length is illegal
   *
   * @throws BufferOverflowException &nbsp; if sink not large enough to hold
   * decoded message body
   */
  def drain(sink: Array[Byte], offset: Int, length: Int): Int =
    drain(ByteBuffer.wrap(sink, offset, length)).position() - offset

  /**
   * Drains decoded message body to supplied sink.
   *
   * @param sink buffer to which message body is written
   *
   * @throws BufferOverflowException &nbsp; if sink not large enough to hold
   * decoded message body
   */
  def drain(sink: ByteBuffer): sink.type =
    BodyDecoder(Int.MaxValue).withDecode(message) { in =>
      val buffer = new Array[Byte](8192)
      var length = 0

      while { length = in.read(buffer); length != -1 } do
        sink.put(buffer, 0, length)
      sink
    }

  /**
   * Drains decoded message body to supplied sink.
   *
   * @param sink      stream to which message body is written
   * @param maxLength maximum number of bytes
   *
   * @throws ReadLimitExceeded if body exceeds `maxLength`
   */
  def drain(sink: OutputStream, maxLength: Long): sink.type =
    drain(Channels.newChannel(sink), maxLength)
    sink

  /**
   * Drains decoded message body to supplied sink.
   *
   * @param sink      channel to which message body is written
   * @param maxLength maximum number of bytes
   *
   * @throws ReadLimitExceeded if body exceeds `maxLength`
   */
  def drain(sink: WritableByteChannel, maxLength: Long): sink.type =
    BodyDecoder(maxLength).withDecode(message) { in =>
      val source = Channels.newChannel(in)
      val buffer = ByteBuffer.allocate(8192)

      while source.read(buffer) != -1 do
        buffer.flip()
        while buffer.hasRemaining() do
          sink.write(buffer)
        buffer.clear()
      sink
    }
