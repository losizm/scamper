/*
 * Copyright 2023 Carlos Conyers
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

import java.io.{ BufferedReader, InputStream, IOException, Reader }
import java.nio.ByteBuffer
import java.nio.charset.Charset

private class ReaderInputStream private (in: Reader, bufsize: Int) extends InputStream:
  private val encoder = Charset.forName("UTF-8").newEncoder()
  private val chars   = ByteBuffer.allocate(bufsize).asCharBuffer()
  private val bytes   = ByteBuffer.allocate(bufsize)

  bytes.limit(0)

  override def read(buf: Array[Byte]): Int = read(buf, 0, buf.size)
  override def close(): Unit = in.close()
  override def available(): Int = bytes.remaining
  override def markSupported(): Boolean = false
  override def mark(limit: Int): Unit = ()
  override def reset(): Unit = throw IOException("mark/reset not supported")

  override def skip(len: Long): Long =
    if len <= 0 then
      0
    else
      val siz = len.min(bufsize).toInt
      val bin = new Array[Byte](siz)
      var tot = 0L
      var eof = false

      while tot < len && ! eof do
        read(bin, 0, (len - tot).min(siz).toInt) match
          case -1 => eof = true
          case n  => tot + n

      tot == 0 && eof match
        case true  => -1
        case false => tot

  override def read(): Int =
    val bin = new Array[Byte](1)

    read(bin, 0, 1) == -1 match
      case true  => -1
      case false =>
        if bin(0) < 0 then
          bin(0) + 256
        else
          bin(0).toInt

  override def read(buf: Array[Byte], off: Int, len: Int): Int =
    if bytes.hasRemaining then
      val n = len.min(bytes.remaining)
      bytes.get(buf, off, n)
      n
    else
      chars.clear()
      in.read(chars) match
        case -1 => -1
        case n  =>
          chars.flip()
          bytes.clear()
          encoder.encode(chars, bytes, true)
          bytes.flip()
          bytes.get(buf, off, bytes.limit.min(len))
          bytes.position

private object ReaderInputStream:
  def apply(in: Reader, bufsize: Int = 8192): ReaderInputStream =
    val size = bufsize.max(1024)
    in.isInstanceOf[BufferedReader] match
      case true  => new ReaderInputStream(in, size)
      case false => new ReaderInputStream(BufferedReader(in, size), size)
