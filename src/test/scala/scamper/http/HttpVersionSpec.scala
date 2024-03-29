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

class HttpVersionSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "HttpVersion" should "be created" in {
    assert(HttpVersion("HTTP/1.0") == HttpVersion(1, 0))
    assert(HttpVersion("HTTP/1.1") == HttpVersion(1, 1))
    assert(HttpVersion("HTTP/2.0") == HttpVersion(2, 0))
    assert(HttpVersion("HTTP/2")   == HttpVersion(2, 0))
  }

  it should "be formatted" in {
    assert(HttpVersion(1, 0).toString == "HTTP/1.0")
    assert(HttpVersion(1, 1).toString == "HTTP/1.1")
    assert(HttpVersion(2, 0).toString == "HTTP/2.0")
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](HttpVersion("1."))
    assertThrows[IllegalArgumentException](HttpVersion(".1"))
    assertThrows[IllegalArgumentException](HttpVersion("1.a"))
    assertThrows[IllegalArgumentException](HttpVersion("a.1"))
    assertThrows[IllegalArgumentException](HttpVersion("a.a"))
    assertThrows[IllegalArgumentException](HttpVersion("2999999999.1"))
    assertThrows[IllegalArgumentException](HttpVersion("1.2999999999"))
    assertThrows[IllegalArgumentException](HttpVersion("2999999999.2999999999"))
  }
