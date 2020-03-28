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

import java.io.IOException

class WriterInputStreamSpec extends org.scalatest.flatspec.AnyFlatSpec {
  private val buf = new Array[Byte](256)

  "WriterInputStream" should "read fully" in {
    val in = new WriterInputStream(8, out => (0 until 256).foreach(out.write))(Auxiliary.executor)
    try assert(in.read(buf) == 256 && in.read() == -1)
    finally in.close()
  }

  it should "skip fully" in {
    val in = new WriterInputStream(8, out => (0 until 256).foreach(out.write))(Auxiliary.executor)
    try assert(in.skip(256) == 256 && in.read() == -1)
    finally in.close()
  }

  it should "throw IOException" in {
    val in = new WriterInputStream(8, { out => (0 until 4).foreach(out.write); throw new Exception })(Auxiliary.executor)
    try assertThrows[IOException](in.read(buf))
    finally in.close()
  }
}
