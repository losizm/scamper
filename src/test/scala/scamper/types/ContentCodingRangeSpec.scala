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
package scamper.types

import org.scalatest.FlatSpec

class ContentCodingRangeSpec extends FlatSpec {
  "ContentCodingRange" should "be created" in {
    var range = ContentCodingRange("GZIP; q=0.7")
    assert(range.name == "gzip")
    assert(range.isGzip)
    assert(range.weight == 0.7f)
    assert(range.toString == "gzip; q=0.7")

    range = ContentCodingRange("*; q=1.0")
    assert(range.name == "*")
    assert(range.isWildcard)
    assert(range.weight == 1.0f)
    assert(range.toString == "*; q=1.0")
  }

  it should "match ContentCoding" in {
    assert(ContentCodingRange("gzip").matches(ContentCoding("gzip")))
    assert(ContentCodingRange("gzip; q=0.6").matches(ContentCoding("gzip")))
    assert(ContentCodingRange("*").matches(ContentCoding("gzip")))
    assert(ContentCodingRange("*").matches(ContentCoding("deflate")))
    assert(ContentCodingRange("*").matches(ContentCoding("compress")))
    assert(ContentCodingRange("*").matches(ContentCoding("identity")))
    assert(ContentCodingRange("*").matches(ContentCoding("other")))
  }

  it should "not match ContentCoding" in {
    assert(!ContentCodingRange("gzip").matches(ContentCoding("compress")))
    assert(!ContentCodingRange("gzip").matches(ContentCoding("deflate")))
    assert(!ContentCodingRange("deflate").matches(ContentCoding("gzip")))
    assert(!ContentCodingRange("deflate").matches(ContentCoding("identity")))
  }

  it should "be destructured" in {
    val range = ContentCodingRange("Deflate; q=0.7")

    range match {
      case ContentCodingRange(name, weight) =>
        assert(name == range.name)
        assert(weight == range.weight)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](ContentCodingRange("identity; q"))
    assertThrows[IllegalArgumentException](ContentCodingRange("identity; q="))
    assertThrows[IllegalArgumentException](ContentCodingRange("identity; =0.1"))
  }
}
