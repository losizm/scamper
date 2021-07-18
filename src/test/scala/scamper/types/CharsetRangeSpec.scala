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
package scamper.types

class CharsetRangeSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "CharsetRange" should "be created" in {
    var range = CharsetRange.parse("utf-8")
    assert(range.charset == "utf-8")
    assert(range.weight == 1f)
    assert(!range.isWildcard)
    assert(range.toString == "utf-8")

    range = CharsetRange.parse("utf-8; q=0.60")
    assert(range.charset == "utf-8")
    assert(range.weight == 0.6f)
    assert(!range.isWildcard)
    assert(range.toString == "utf-8; q=0.6")

    range = CharsetRange.parse("*; q=1.0")
    assert(range.charset == "*")
    assert(range.weight == 1.0f)
    assert(range.isWildcard)
    assert(range.toString == "*")
  }

  it should "match Charset" in {
    assert(CharsetRange.parse("utf-8").matches("UTF-8"))
    assert(CharsetRange.parse("UTF-8; q=0.6").matches("utf-8"))
    assert(CharsetRange.parse("*").matches("UTF-8"))
    assert(CharsetRange.parse("*").matches("iso-8859-1"))
    assert(CharsetRange.parse("*; q=0.001").matches("ascii"))
  }

  it should "not match Charset" in {
    assert(!CharsetRange.parse("utf-8").matches("utf-16"))
    assert(!CharsetRange.parse("utf-16be; q=0.6").matches("utf-16le"))
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](CharsetRange.parse("ascii; q"))
    assertThrows[IllegalArgumentException](CharsetRange.parse("ascii; q="))
    assertThrows[IllegalArgumentException](CharsetRange.parse("ascii; =0.1"))
  }
