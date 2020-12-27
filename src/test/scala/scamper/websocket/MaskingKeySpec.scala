/*
 * Copyright 2020 Carlos Conyers
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

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.util.Arrays

class MaskingKeySpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "mask/unmask data" in {
    implicit val key = MaskingKey()
    assert(key.value != 0)

    val msg     = "Peter piper picked peters, but Run rocked rhymes."
    val orig    = msg.getBytes("utf-8")
    val bulk1   = bulkMask(msg.getBytes("utf-8"), 1)
    val bulk2   = bulkMask(msg.getBytes("utf-8"), 2)
    val stream1 = streamMask(msg.getBytes("utf-8"), 1)
    val stream2 = streamMask(msg.getBytes("utf-8"), 2)

    assert(!Arrays.equals(bulk1, orig))
    assert(Arrays.equals(bulk2, orig))

    assert(!Arrays.equals(stream1, orig))
    assert(Arrays.equals(stream2, orig))

    assert(Arrays.equals(bulk1, stream1))
    assert(Arrays.equals(bulk2, stream2))
  }

  @annotation.tailrec
  private def bulkMask(data: Array[Byte], times: Int)(implicit key: MaskingKey): Array[Byte] =
    times < 1 match {
      case true  => data
      case false =>
        key(data)
        bulkMask(data, times - 1)
    }

  @annotation.tailrec
  private def streamMask(data: Array[Byte], times: Int)(implicit key: MaskingKey): Array[Byte] =
    times < 1 match {
      case true  => data
      case false =>
        val in  = new ByteArrayInputStream(data)
        val out = new ByteArrayOutputStream()
        var buf = new Array[Byte](5) // Use uneven buffer for test
        var len = 0
        var pos = 0

        while ({ len = in.read(buf); len != -1 }) {
          key(buf, len, pos)
          out.write(buf, 0, len)
          pos += len
        }

        streamMask(out.toByteArray, times - 1)
    }
}
