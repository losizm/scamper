/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.time.{ Instant, LocalDate }

import Auxiliary._

class AuxiliarySpec extends org.scalatest.flatspec.AnyFlatSpec {
  val uri = Uri("http://localhost:8080/index.html")
  val uriPath = Uri("/index.html")
  val uriPathWithQuery = Uri("/index.html?name=guest")

  "URI" should "be created with new path" in {
    assert(uriPath.withPath("/home.html") == Uri("/home.html"))
  }

  it should "be created with new query" in {
    assert(uriPath.withQuery("name=guest") == uriPathWithQuery)
  }

  it should "created with new scheme and authority" in {
    assert(uriPath.withScheme("http").withAuthority("localhost:8080") == uri)
  }

  "String" should "be converted to Instant" in {
    assert("2006-02-15T04:15:37Z".toInstant == Instant.parse("2006-02-15T04:15:37Z"))
    assert("Wed, 15 Feb 2006 04:15:37 GMT".toInstant == Instant.parse("2006-02-15T04:15:37Z"))
  }

  it should "match at least one regular expression" in {
    assert("abc".matchesAny("a.c", "123", "xyz"))
    assert("abc".matchesAny("123", "a.c", "xyz"))
    assert("abc".matchesAny("123", "a.*", "xyz"))
    assert("abc".matchesAny("a.c"))
  }

  it should "not match any regular expression" in {
    assert(!"XYZ".matchesAny("a.c", "123", "xyz"))
    assert(!"XYZ".matchesAny("123", "a.c", "xyz"))
    assert(!"XYZ".matchesAny("123", "a.*", "xyz"))
    assert(!"XYZ".matchesAny())
  }
}
