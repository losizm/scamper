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

import java.io.IOException

import org.scalatest.FlatSpec

class WriterInputStreamSpec extends FlatSpec {
  "WriterInputStream" should "be read fully" in {
    val writer = new WriterInputStream(8, out => (0 until 256).foreach(out.write))(Auxiliary.executor)
    val buffer = new Array[Byte](256)
    assert(writer.read(buffer) == 256)
    assert(writer.read(buffer) == -1)
  }

  it should "throw IOException" in {
    val writer = new WriterInputStream(8, { out =>
      (0 until 8).foreach(out.write)
      throw new RuntimeException
    })(Auxiliary.executor)

    val buffer = new Array[Byte](8)
    assert(writer.read(buffer) == 8)
    assertThrows[IOException](writer.read(buffer))
  }
}
