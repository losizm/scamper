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

class TransferCodingRangeSpec extends FlatSpec {
  "TransferCodingRange" should "be created without parameters" in {
    var range = TransferCodingRange("CHUNKED")
    assert(range.name == "chunked")
    assert(range.isChunked)
    assert(range.rank == 1.0f)
    assert(range.params.isEmpty)
    assert(range.toString == "chunked; q=1.0")

    range = TransferCodingRange("X-GZIP; q=0.7")
    assert(range.name == "gzip")
    assert(range.isGzip)
    assert(range.rank == 0.7f)
    assert(range.params.isEmpty)
    assert(range.toString == "gzip; q=0.7")
  }

  it should "be created with parameters" in {
    var range = TransferCodingRange("CHUNKED; x=abc")
    assert(range.name == "chunked")
    assert(range.isChunked)
    assert(range.rank == 1.0f)
    assert(range.params("x") == "abc")
    assert(range.toString == "chunked; q=1.0; x=abc")

    range = TransferCodingRange("""GZIP; q=0.1; level="1 2 3" """)
    assert(range.name == "gzip")
    assert(range.isGzip)
    assert(range.rank == 0.1f)
    assert(range.params("level") == "1 2 3")
    assert(range.toString == "gzip; q=0.1; level=\"1 2 3\"")
  }

  it should "match TransferCoding" in {
    assert(TransferCodingRange("chunked; q=1.0; x=0").matches(TransferCoding("chunked; x=0; y=1")))
    assert(TransferCodingRange("chunked; q=1.0; x=0; y=1").matches(TransferCoding("chunked; x=0; y=1")))
    assert(TransferCodingRange("gzip").matches(TransferCoding("gzip")))
    assert(TransferCodingRange("gzip").matches(TransferCoding("gzip; x=0")))
  }

  it should "not match TransferCoding" in {
    assert(!TransferCodingRange("chunked; q=1.0; x=0").matches(TransferCoding("chunked; y=1")))
    assert(!TransferCodingRange("chunked; q=1.0; x=0; y=1").matches(TransferCoding("chunked; x=0")))
    assert(!TransferCodingRange("gzip; y=1").matches(TransferCoding("gzip; x=0")))
    assert(!TransferCodingRange("gzip; y=1").matches(TransferCoding("gzip")))
  }

  it should "be destructured" in {
    val range = TransferCodingRange("""Deflate; a=1; b=two; c="x y z" """)

    range match {
      case TransferCodingRange(name, rank, params) =>
        assert(name == range.name)
        assert(rank == range.rank)
        assert(params == range.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](TransferCodingRange("chunked; q"))
    assertThrows[IllegalArgumentException](TransferCodingRange("chunked; q="))
    assertThrows[IllegalArgumentException](TransferCodingRange("chunked; =0.1"))
  }
}

