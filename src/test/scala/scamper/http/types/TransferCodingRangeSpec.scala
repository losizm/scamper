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
package scamper
package http
package types

class TransferCodingRangeSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "TransferCodingRange" should "be created without parameters" in {
    var range = TransferCodingRange.parse("CHUNKED; q=1.0")
    assert(range.name == "chunked")
    assert(range.isChunked)
    assert(range.weight == 1.0f)
    assert(range.params.isEmpty)
    assert(range.toString == "chunked")

    range = TransferCodingRange.parse("X-GZIP; q=0.7")
    assert(range.name == "gzip")
    assert(range.isGzip)
    assert(range.weight == 0.7f)
    assert(range.params.isEmpty)
    assert(range.toString == "gzip; q=0.7")
  }

  it should "be created with parameters" in {
    var range = TransferCodingRange.parse("CHUNKED; x=abc")
    assert(range.name == "chunked")
    assert(range.isChunked)
    assert(range.weight == 1.0f)
    assert(range.params("x") == "abc")
    assert(range.toString == "chunked; x=abc")

    range = TransferCodingRange.parse("""GZIP; q=0.1; level="1 2 3" """)
    assert(range.name == "gzip")
    assert(range.isGzip)
    assert(range.weight == 0.1f)
    assert(range.params("level") == "1 2 3")
    assert(range.toString == "gzip; q=0.1; level=\"1 2 3\"")
  }

  it should "match TransferCoding" in {
    assert(TransferCodingRange.parse("chunked; q=1.0; x=0").matches(TransferCoding.parse("chunked; x=0; y=1")))
    assert(TransferCodingRange.parse("chunked; q=1.0; x=0; y=1").matches(TransferCoding.parse("chunked; x=0; y=1")))
    assert(TransferCodingRange.parse("gzip").matches(TransferCoding.parse("gzip")))
    assert(TransferCodingRange.parse("gzip").matches(TransferCoding.parse("gzip; x=0")))
  }

  it should "not match TransferCoding" in {
    assert(!TransferCodingRange.parse("chunked; q=1.0; x=0").matches(TransferCoding.parse("chunked; y=1")))
    assert(!TransferCodingRange.parse("chunked; q=1.0; x=0; y=1").matches(TransferCoding.parse("chunked; x=0")))
    assert(!TransferCodingRange.parse("gzip; y=1").matches(TransferCoding.parse("gzip; x=0")))
    assert(!TransferCodingRange.parse("gzip; y=1").matches(TransferCoding.parse("gzip")))
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](TransferCodingRange.parse("chunked; q"))
    assertThrows[IllegalArgumentException](TransferCodingRange.parse("chunked; q="))
    assertThrows[IllegalArgumentException](TransferCodingRange.parse("chunked; =0.1"))
  }
