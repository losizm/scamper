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

import java.net.Socket

private implicit class SocketExtensions(socket: Socket) extends AnyVal:
  def read(): Int =
    socket.getInputStream.read()

  def read(buffer: Array[Byte]): Int =
    socket.getInputStream.read(buffer)

  def read(buffer: Array[Byte], offset: Int, length: Int): Int =
    socket.getInputStream.read(buffer, offset, length)

  def readLine(buffer: Array[Byte], offset: Int = 0): Int =
    socket.getInputStream.readLine(buffer, offset)

  def getToken(delimiters: String, buffer: Array[Byte], offset: Int = 0): String =
    socket.getInputStream.getToken(delimiters, buffer, offset)

  def getLine(buffer: Array[Byte], offset: Int = 0): String =
    socket.getInputStream.getLine(buffer, offset)

  def write(byte: Int): Unit =
    socket.getOutputStream.write(byte)

  def write(buffer: Array[Byte]): Unit =
    socket.getOutputStream.write(buffer)

  def write(buffer: Array[Byte], offset: Int, length: Int): Unit =
    socket.getOutputStream.write(buffer, offset, length)

  def writeLine(text: String): Unit =
    socket.getOutputStream.writeLine(text)

  def writeLine(): Unit =
    socket.getOutputStream.writeLine()

  def flush(): Unit =
    socket.getOutputStream.flush()
