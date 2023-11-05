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

import java.io.*

class ReaderInputStreamSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "read file" in {
    val file = File("src/main/scala/scamper/http/websocket/WebSocketSessionImpl.scala")
    val in   = ReaderInputStream(FileReader(file))

    try
      val buf = new Array[Byte](128)
      var tot = 0
      var len = in.read(buf)

      while len != -1 do
        tot += len
        len = in.read(buf)

      info(s"File size:  ${file.length}")
      info(s"Bytes read: $tot")

      assert(tot == file.length)
    finally
      in.close()
  }

  it should "read file on byte at a time" in {
    val file = File("src/main/scala/scamper/http/websocket/WebSocketSessionImpl.scala")
    val in   = ReaderInputStream(FileReader(file))

    try
      var tot = 0
      while in.read() != -1 do
        tot += 1

      info(s"File size:  ${file.length}")
      info(s"Bytes read: $tot")

      assert(tot == file.length)
    finally
      in.close()
  }
