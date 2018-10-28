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

class TransferCodingSpec extends FlatSpec {
  "TransferCoding" should "be created without parameters" in {
    var coding = TransferCoding.parse("CHUNKED")
    assert(coding.name == "chunked")
    assert(coding.isChunked)
    assert(coding.params.isEmpty)
    assert(coding.toString == "chunked")

    coding = TransferCoding.parse("GZIP")
    assert(coding.name == "gzip")
    assert(coding.isGzip)
    assert(coding.params.isEmpty)
    assert(coding.toString == "gzip")
  }

  it should "be created with parameters" in {
    var coding = TransferCoding.parse("CHUNKED; q=0.1")
    assert(coding.name == "chunked")
    assert(coding.isChunked)
    assert(coding.params("q").equals("0.1"))
    assert(coding.toString == "chunked; q=0.1")

    coding = TransferCoding.parse("""GZIP; q=0.1; level="1 2 3" """)
    assert(coding.name == "gzip")
    assert(coding.isGzip)
    assert(coding.params("q").equals("0.1"))
    assert(coding.params("level").equals("1 2 3"))
    assert(coding.toString == "gzip; q=0.1; level=\"1 2 3\"" || coding.toString == "gzip; level=\"1 2 3\"; q=0.1")
  }

  it should "be destructured" in {
    val coding = TransferCoding.parse("""Deflate; a=1; b=two; c="x y z" """)

    coding match {
      case TransferCoding(name, params) =>
        assert(name == coding.name)
        assert(params == coding.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](TransferCoding.parse("chunked; q"))
    assertThrows[IllegalArgumentException](TransferCoding.parse("chunked; q="))
    assertThrows[IllegalArgumentException](TransferCoding.parse("chunked; =0.1"))
  }
}
