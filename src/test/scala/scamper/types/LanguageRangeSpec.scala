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

class LanguageRangeSpec extends FlatSpec {
  "LanguageRange" should "be created" in {
    var range = LanguageRange.parse("en")
    assert(range.tag == "en")
    assert(range.weight == 1f)
    assert(range.toString == "en")

    range = LanguageRange.parse("en; q=0.5")
    assert(range.tag == "en")
    assert(range.weight == 0.5f)
    assert(range.toString == "en; q=0.5")

    range = LanguageRange.parse("en-US-1995; q=0.123456789")
    assert(range.tag == "en-US-1995")
    assert(range.weight == 0.123f)
    assert(range.toString == "en-US-1995; q=0.123")
  }

  it should "match LanguageTag" in {
    assert(LanguageRange.parse("en").matches(LanguageTag.parse("EN")))
    assert(LanguageRange.parse("en; q=0.1").matches(LanguageTag.parse("en-US")))
    assert(LanguageRange.parse("en-US").matches(LanguageTag.parse("en-US")))
    assert(LanguageRange.parse("en-US; q=0.5").matches(LanguageTag.parse("en-US-1995")))
  }

  it should "not match LanguageTag" in {
    assert(!LanguageRange.parse("en-US; q=0.1").matches(LanguageTag.parse("en")))
    assert(!LanguageRange.parse("en-US-1995").matches(LanguageTag.parse("en-US")))
    assert(!LanguageRange.parse("en").matches(LanguageTag.parse("fr")))
  }

  it should "be destructured" in {
    val range = LanguageRange.parse("en-US-1995; q=0.6")

    range match {
      case LanguageRange(tag, weight) =>
        assert(tag == range.tag)
        assert(weight == range.weight)
    }
  }

  it should "not be created with invalid name" in {
    assertThrows[IllegalArgumentException](LanguageRange.parse("en-US; q="))
    assertThrows[IllegalArgumentException](LanguageRange.parse("1995-en-US; q=1.0"))
  }
}
