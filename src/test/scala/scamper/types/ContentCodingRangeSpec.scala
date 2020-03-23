/*
 * Copyright 2019 Carlos Conyers
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

class ContentCodingRangeSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "ContentCodingRange" should "be created" in {
    var range = ContentCodingRange.parse("GZIP; q=0.7")
    assert(range.name == "gzip")
    assert(range.isGzip)
    assert(range.weight == 0.7f)
    assert(range.toString == "gzip; q=0.7")

    range = ContentCodingRange.parse("*; q=1.0")
    assert(range.name == "*")
    assert(range.isWildcard)
    assert(range.weight == 1.0f)
    assert(range.toString == "*")
  }

  it should "match ContentCoding" in {
    assert(ContentCodingRange.parse("gzip").matches(ContentCoding("gzip")))
    assert(ContentCodingRange.parse("gzip; q=0.6").matches(ContentCoding("gzip")))
    assert(ContentCodingRange.parse("*").matches(ContentCoding("gzip")))
    assert(ContentCodingRange.parse("*").matches(ContentCoding("deflate")))
    assert(ContentCodingRange.parse("*").matches(ContentCoding("compress")))
    assert(ContentCodingRange.parse("*").matches(ContentCoding("identity")))
    assert(ContentCodingRange.parse("*").matches(ContentCoding("other")))
  }

  it should "not match ContentCoding" in {
    assert(!ContentCodingRange.parse("gzip").matches(ContentCoding("compress")))
    assert(!ContentCodingRange.parse("gzip").matches(ContentCoding("deflate")))
    assert(!ContentCodingRange.parse("deflate").matches(ContentCoding("gzip")))
    assert(!ContentCodingRange.parse("deflate").matches(ContentCoding("identity")))
  }

  it should "be destructured" in {
    val range = ContentCodingRange.parse("Deflate; q=0.7")

    range match {
      case ContentCodingRange(name, weight) =>
        assert(name == range.name)
        assert(weight == range.weight)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](ContentCodingRange.parse("identity; q"))
    assertThrows[IllegalArgumentException](ContentCodingRange.parse("identity; q="))
    assertThrows[IllegalArgumentException](ContentCodingRange.parse("identity; =0.1"))
  }
}
