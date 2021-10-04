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

class HeaderSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "Header" should "be created from formatted value" in {
    val header = Header("Content-Type: text/plain")
    assert(header.name == "Content-Type")
    assert(header.value == "text/plain")
    assert(header.toString == "Content-Type: text/plain")
  }

  it should "be created using int value" in {
    val header = Header("Content-Length", 80)
    assert(header.name == "Content-Length")
    assert(header.value == "80")
    assert(header.intValue == 80)
    assert(header.toString == "Content-Length: 80")
  }

  it should "be created using long value" in {
    val header = Header("Content-Length", 80L)
    assert(header.name == "Content-Length")
    assert(header.value == "80")
    assert(header.longValue == 80)
    assert(header.toString == "Content-Length: 80")
  }

  it should "be created using date value" in {
    val header = Header("If-Modified-Since", DateValue.parse("Tue, 8 Nov 2016 21:00:00 -0500"))
    assert(header.name == "If-Modified-Since")
    assert(header.value == "Wed, 9 Nov 2016 02:00:00 GMT")
    assert(header.dateValue == DateValue.parse("Wed, 9 Nov 2016 02:00:00 GMT"))
    assert(header.toString == "If-Modified-Since: Wed, 9 Nov 2016 02:00:00 GMT")
  }

  it should "be created using folded value" in {
    val header = Header("Cookie", "user=guest,\r\n\tgroup=readonly")
    assert(header.name == "Cookie")
    assert(header.value == "user=guest,\r\n\tgroup=readonly")
  }

  it should "not be created from malformed value" in {
    assertThrows[IllegalArgumentException](Header("text/plain"))
    assertThrows[IllegalArgumentException](Header("Cookie", "user=guest,\r\ngroup=readonly"))
  }
