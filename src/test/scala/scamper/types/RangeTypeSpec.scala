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
package scamper.types

import scamper.types.ByteRange._

class RangeTypeSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "ByteRange" should "be created with single spec" in {
    var range = ByteRange.parse("bytes=0-9")
    assert(range.unit == "bytes")
    assert(range.set(0).asInstanceOf[Slice] == Slice(0, Some(9)))
    assert(range.toString == "bytes=0-9")

    range = ByteRange.parse("bytes=100-")
    assert(range.unit == "bytes")
    assert(range.set(0).asInstanceOf[Slice] == Slice(100, None))
    assert(range.toString == "bytes=100-")

    range = ByteRange.parse("bytes=-1024")
    assert(range.unit == "bytes")
    assert(range.set(0).asInstanceOf[Suffix] == Suffix(1024))
    assert(range.toString == "bytes=-1024")
  }

  it should "be created with multiple spec" in {
    var range = ByteRange.parse("bytes=0-9,100-124")
    assert(range.unit == "bytes")
    assert(range.set(0).asInstanceOf[Slice] == Slice(0, Some(9)))
    assert(range.set(1).asInstanceOf[Slice] == Slice(100, Some(124)))
    assert(range.toString == "bytes=0-9,100-124")

    range = ByteRange.parse("bytes=0-9,100-124,-256")
    assert(range.unit == "bytes")
    assert(range.set(0).asInstanceOf[Slice] == Slice(0, Some(9)))
    assert(range.set(1).asInstanceOf[Slice] == Slice(100, Some(124)))
    assert(range.set(2).asInstanceOf[Suffix] == Suffix(256))
    assert(range.toString == "bytes=0-9,100-124,-256")
  }
}
