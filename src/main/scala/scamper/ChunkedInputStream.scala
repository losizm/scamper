/*
 * Copyright 2018 Carlos Conyers
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

import java.io.{ InputStream, IOException }

private class ChunkedInputStream(in: InputStream) extends InputStream {
  private var chunkSize = 0
  private var position = 0

  nextChunk()

  override def read(): Int = withReadability {
    case true  => position += 1; in.read()
    case false => -1
  }

  override def read(buf: Array[Byte]): Int = read(buf, 0, buf.length)

  override def read(buf: Array[Byte], off: Int, len: Int): Int = withReadability {
    case true =>
      if (off < 0 || len < 0 || (off + len) > buf.length)
        throw new IndexOutOfBoundsException()

      in.read(buf, off, len.min(chunkSize - position)) match {
        case -1 => throw new HttpException("Truncation detected")
        case n  =>
          position += n
          n
      }

    case false => -1
  }

  override def available(): Int = withReadability {
    case true  => chunkSize - position
    case false => 0
  }

  override def skip(count: Long): Long = withReadability {
    case true =>
      if (count <= 0) 0
      else {
        val len = count.min(8192).toInt
        val buf = new Array[Byte](len)
        read(buf).max(0)
      }

    case false => 0
  }

  override def markSupported(): Boolean = false
  override def mark(limit: Int): Unit = ()
  override def reset(): Unit = throw new IOException("Mark/reset not supported")
  override def close(): Unit = in.close()

  private def withReadability[T](f: Boolean => T): T = {
    if (position == chunkSize && chunkSize > 0)
      nextChunk()
    f(position < chunkSize)
  }

  private def nextChunk(): Unit = {
    chunkSize = nextChunkSize
    position = 0
  }

  private def nextChunkSize: Int = {
    if (chunkSize > 0 && readLine().length != 0)
      throw new HttpException("Invalid chunk termination")

    val regex = "(\\d+)(\\s*;\\s*.+=.+)*".r

    readLine match {
      case regex(size, _*) => size.toInt
      case line => throw new HttpException(s"Invalid chunk size: $line")
    }
  }

  private def readLine(): String = {
    val buf = new Array[Byte](256)
    var byte = in.read()
    var len = 0

    while (byte != '\n' && byte != -1) {
      buf(len) = byte.toByte
      byte = in.read()
      len += 1
    }

    if (len > 0 && buf(len - 1) == '\r')
      len -= 1

    new String(buf, 0, len)
  }
}
