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

import java.io.InputStream

private implicit class InputStreamExtensions(in: InputStream) extends AnyVal:
  def getToken(delimiters: String, buffer: Array[Byte], offset: Int = 0): String =
    var length = offset
    var byte   = in.read()

    while byte != -1 && !delimiters.contains(byte) do
      buffer(length) = byte.toByte
      length        += 1
      byte           = in.read()

    String(buffer, 0, length, "UTF-8")

  def getLine(buffer: Array[Byte], offset: Int = 0): String =
    var length = offset
    var byte   = in.read()

    while byte != -1 && byte != '\n' do
      buffer(length) = byte.toByte
      length        += 1
      byte           = in.read()

    if length > 0 && buffer(length - 1) == '\r' then
      length -= 1

    String(buffer, 0, length, "UTF-8")

  def readLine(buffer: Array[Byte], offset: Int = 0): Int =
    val bufferSize = buffer.size
    var length     = offset
    var continue   = length < bufferSize

    while continue do
      in.read() match
        case -1 =>
          continue = false

        case byte =>
          buffer(length) = byte.toByte
          length  += 1
          continue = length < bufferSize && byte != '\n'

    length

  def readMostly(buffer: Array[Byte]): Int =
    readMostly(buffer, 0, buffer.size)

  def readMostly(buffer: Array[Byte], offset: Int, length: Int): Int =
    var total = in.read(buffer, offset, length)

    if total != -1 && total < length then
      var count = 0

      while count != -1 && total < length do
        total += count
        count  = in.read(buffer, offset + total, length - total)

    total
