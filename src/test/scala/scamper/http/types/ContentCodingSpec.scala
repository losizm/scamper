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

class ContentCodingSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "ContentCoding" should "be created" in {
    var coding = ContentCoding("Compress")
    assert(coding.name == "compress")
    assert(coding.isCompress)
    assert(coding.toString == "compress")

    coding = ContentCoding("X-COMPRESS")
    assert(coding.name == "compress")
    assert(coding.isCompress)
    assert(coding.toString == "compress")

    coding = ContentCoding("GZIP")
    assert(coding.name == "gzip")
    assert(coding.isGzip)
    assert(coding.toString == "gzip")

    coding = ContentCoding("x-GZIP")
    assert(coding.name == "gzip")
    assert(coding.isGzip)
    assert(coding.toString == "gzip")
  }

  it should "not be created with invalid name" in {
    assertThrows[IllegalArgumentException](ContentCoding("x gzip"))
    assertThrows[IllegalArgumentException](ContentCoding("gzip; q=1.0"))
  }
