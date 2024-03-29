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

import java.time.{ Instant, LocalDate }

class StringExtensionsSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "match string with at least one regular expression" in {
    assert("abc".matchesAny("a.c", "123", "xyz"))
    assert("abc".matchesAny("123", "a.c", "xyz"))
    assert("abc".matchesAny("123", "a.*", "xyz"))
    assert("abc".matchesAny("a.c"))
  }

  it should "not match string with any regular expression" in {
    assert(!"XYZ".matchesAny("a.c", "123", "xyz"))
    assert(!"XYZ".matchesAny("123", "a.c", "xyz"))
    assert(!"XYZ".matchesAny("123", "a.*", "xyz"))
    assert(!"XYZ".matchesAny())
  }
