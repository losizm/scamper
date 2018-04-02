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

import java.io.{ ByteArrayInputStream, InputStream }
import java.util.Enumeration

import scala.collection.mutable.ArrayBuffer

private class ChunkEnumeration(in: InputStream, maxChunkSize: Int, maxTotalLength: Long) extends Enumeration[InputStream] {
  private val chunkLine = "(\\d+)(\\s*;\\s*.+=.+)*".r
  private var chunkSize = nextChunkSize
  private var totalLength = chunkSize

  def hasMoreElements(): Boolean = chunkSize > 0

  def nextElement(): InputStream = {
    if (!hasMoreElements) throw new NoSuchElementException("No more chunks")
    if (chunkSize > maxChunkSize) throw new HttpException(s"Chunk too large: $chunkSize > $maxChunkSize")
    if (totalLength > maxTotalLength) throw new HttpException(s"Entity too large: $totalLength > $maxTotalLength")

    val buffer = new Array[Byte](chunkSize)
    var length = 0

    while (length < chunkSize)
      in.read(buffer, length, chunkSize - length) match {
        case -1    => throw new HttpException("Unexpected end of entity")
        case count => length += count
      }

    // discard CRLF
    if (nextLine.length != 0) throw new HttpException("Invalid chunk termination")

    chunkSize = nextChunkSize
    totalLength += chunkSize

    new ByteArrayInputStream(buffer)
  }

  private def nextChunkSize: Int =
    nextLine match {
      case chunkLine(size, _*) => size.toInt
      case line => throw new HttpException(s"Invalid chunk size: $line")
    }

  private def nextLine: String = {
    def nextByte: Int =
      in.read() match {
        case '\r' =>
          if (in.read() != '\n') throw new HttpException("Invalid line termination in chunk")
          else -1

        case '\n' => -1
        case byte => byte
      }

    val buffer = new ArrayBuffer[Byte](8)
    var byte = 0

    while ({ byte = nextByte; byte != -1 })
      buffer += byte.toByte

    new String(buffer.toArray, "ASCII")
  }
}

