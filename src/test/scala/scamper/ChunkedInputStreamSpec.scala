/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.io.{ ByteArrayInputStream, EOFException, IOException }

class ChunkedInputStreamSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "ChunkedInputStream" should "read bytes" in {
    val chunks = "13\r\nToo much wagonist. \r\n14\r\nToo much antagonist.\r\n0\r\n\r\n"
    var in = new ChunkedInputStream(new ByteArrayInputStream(chunks.getBytes("UTF-8")))
    val buffer = new Array[Byte](80)
    val length = in.read(buffer)
    val text = new String(buffer, 0, length, "UTF-8")

    assert(text == "Too much wagonist. Too much antagonist.")
  }

  it should "skip bytes" in {
    val chunks = "13\r\nToo much wagonist. \r\n14\r\nToo much antagonist.\r\n0\r\n\r\n"
    var in = new ChunkedInputStream(new ByteArrayInputStream(chunks.getBytes("UTF-8")))
    assert(in.skip(10) == 10)

    in = new ChunkedInputStream(new ByteArrayInputStream(chunks.getBytes("UTF-8")))
    assert(in.skip(25) == 25)

    in = new ChunkedInputStream(new ByteArrayInputStream(chunks.getBytes("UTF-8")))
    assert(in.skip(39) == 39)

    in = new ChunkedInputStream(new ByteArrayInputStream(chunks.getBytes("UTF-8")))
    assert(in.skip(99) == 39)

    in = new ChunkedInputStream(new ByteArrayInputStream(chunks.getBytes("UTF-8")))
    assert(in.skip(-5) == 0)
  }

  it should "be invalid" in {
    val chunks = "10\r\nToo much wagonist. \r\n14\r\nToo much antagonist.\r\n0\r\n\r\n"
    val in = new ChunkedInputStream(new ByteArrayInputStream(chunks.getBytes("UTF-8")))
    assertThrows[IOException](in.skip(100))
  }

  it should "be truncated" in {
    val chunks = "99\r\nToo much wagonist. \r\n14\r\nToo much antagonist.\r\n0\r\n\r\n"
    val in = new ChunkedInputStream(new ByteArrayInputStream(chunks.getBytes("UTF-8")))
    assertThrows[EOFException](in.skip(100))
  }
}
