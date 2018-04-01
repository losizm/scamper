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

class LanguageTagSpec extends FlatSpec {
  "LanguageTag" should "be created" in {
    var tag = LanguageTag("en")
    assert(tag.primary == "en")
    assert(tag.toString == "en")
    assert(tag.toRange(1.0f).tag == "en") 
    assert(tag.toRange(1.0f).weight == 1f) 

    tag = LanguageTag("en-US")
    assert(tag.primary == "en")
    assert(tag.others == Seq("US"))
    assert(tag.toString == "en-US")
    assert(tag.toRange(0.5f).tag == "en-US") 
    assert(tag.toRange(0.5f).weight == 0.5f) 

    tag = LanguageTag("en-US-1995")
    assert(tag.primary == "en")
    assert(tag.others == Seq("US", "1995"))
    assert(tag.toString == "en-US-1995")
    assert(tag.toRange(0.123f).tag == "en-US-1995") 
    assert(tag.toRange(0.123f).weight == 0.123f) 
  }

  it should "be destructured" in {
    val tag = LanguageTag("en-US-1995")
    tag match {
      case LanguageTag(primary, Seq(other1, other2)) =>
        assert(primary == tag.primary)
        assert(other1 == tag.others(0))
        assert(other2 == tag.others(1))
    }
  }

  it should "not be created with invalid name" in {
    assertThrows[IllegalArgumentException](LanguageTag("en US"))
    assertThrows[IllegalArgumentException](LanguageTag("1995-en-US"))
  }
}

