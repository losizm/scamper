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
package scamper

import java.io.{ EOFException, InputStream, IOException }

private class ChunkedInputStream(in: InputStream) extends InputStream {
  private var chunkSize = 0
  private var position = 0

  nextChunk()

  override def read(): Int = isReadable() match {
    case true =>
      in.read() match {
        case -1   => throw new EOFException("Truncation detected")
        case byte => position += 1; byte
      }
    case false => -1
  }

  override def read(buffer: Array[Byte]): Int = read(buffer, 0, buffer.length)

  override def read(buffer: Array[Byte], offset: Int, length: Int): Int = isReadable() match {
    case true =>
      var total = 0
      while (total < length && isReadable())
        in.read(buffer, offset + total, (length - total).min(chunkSize - position)) match {
          case -1    => throw new EOFException("Truncation detected")
          case count => total += count; position += count
        }
      total
    case false => -1
  }

  override def available(): Int = isReadable() match {
    case true  => chunkSize - position
    case false => 0
  }

  override def skip(count: Long): Long = isReadable() match {
    case true =>
      val buffer = new Array[Byte](count.max(0).min(8192).toInt)
      var skipCount = 0L
      while (skipCount < count && isReadable())
        skipCount += read(buffer, 0, (count - skipCount).min(buffer.length).toInt)
      skipCount
    case false => 0
  }

  override def markSupported(): Boolean = false
  override def mark(limit: Int): Unit = ()
  override def reset(): Unit = throw new IOException("Mark/reset not supported")
  override def close(): Unit = in.close()

  private def isReadable(): Boolean = {
    if (position == chunkSize && chunkSize > 0)
      nextChunk()
    position < chunkSize
  }

  private def nextChunk(): Unit = {
    chunkSize = nextChunkSize
    position = 0
  }

  private def nextChunkSize: Int = {
    if (chunkSize > 0 && readLine().length != 0)
      throw new IOException("Invalid chunk termination")

    val regex = "(\\p{XDigit}+)(\\s*;\\s*.+=.+)*".r

    readLine match {
      case regex(size, _) =>
        Integer.parseInt(size, 16) match {
          case 0 => readLine(); 0
          case n => n
        }
      case line => throw new IOException(s"Invalid chunk size: $line")
    }
  }

  private def readLine(): String = {
    val buffer = new Array[Byte](256)
    var byte = in.read()
    var length = 0

    while (byte != '\n' && byte != -1) {
      buffer(length) = byte.toByte
      byte = in.read()
      length += 1
    }

    if (length > 0 && buffer(length - 1) == '\r')
      length -= 1

    new String(buffer, 0, length)
  }
}
