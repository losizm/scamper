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

import scamper.types.ByteContentRange.*

class ContentRangeTypeSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "ByteContentRange" should "be created with satisfied response" in {
    var range = ByteContentRange.parse("bytes 8-15/1024")
    assert(range.unit == "bytes")
    assert(range.resp.asInstanceOf[Satisfied] == Satisfied(8, 15, Some(1024)))
    assert(range.toString == "bytes 8-15/1024")

    range = ByteContentRange.parse("bytes 8-15/*")
    assert(range.unit == "bytes")
    assert(range.resp.asInstanceOf[Satisfied] == Satisfied(8, 15, None))
    assert(range.toString == "bytes 8-15/*")
  }

  it should "be created with unsatisfied response" in {
    val range = ByteContentRange.parse("bytes */8192")
    assert(range.unit == "bytes")
    assert(range.resp.asInstanceOf[Unsatisfied] == Unsatisfied(8192))
    assert(range.toString == "bytes */8192")
  }
