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

import Preferences.*

class PreferenceSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "Preference" should "be created name, value, and parameters" in {
    val pref = Preference.parse("Pref=value; param1=\"value1\"; param2=value2; param3=\"value three\"")
    assert(pref.name == "pref")
    assert(pref.value.contains("value"))
    assert(pref.params.size == 3)
    assert(pref.params("param1").contains("value1"))
    assert(pref.params("param2").contains("value2"))
    assert(pref.params("param3").contains("value three"))
    assert(pref.toString == "pref=value; param1=value1; param2=value2; param3=\"value three\"")
  }

  it should "be created with name and value only" in {
    val pref = Preference.parse("Pref=value")
    assert(pref.name == "pref")
    assert(pref.value.contains("value"))
    assert(pref.params.isEmpty)
    assert(pref.toString == "pref=value")
  }

  it should "be created with name only" in {
    val pref = Preference.parse("PREF")
    assert(pref.name == "pref")
    assert(pref.value.isEmpty)
    assert(pref.params.isEmpty)
    assert(pref.toString == "pref")
  }

  it should "be created as registered preference" in {
    assert(Preference.parse("WAIT=90") == `wait=duration`(90))
    assert(Preference("wait", "60") == `wait=duration`(60))
    assert(Preference("Wait", "30", Map("stop" -> None)) == `wait=duration`(30))

    assert(Preference.parse("HANDLING=strict") == `handling=strict`)
    assert(Preference("handling", "strict") == `handling=strict`)
    assert(Preference("handling", "strict", Map("stop" -> None)) == `handling=strict`)

    assert(Preference.parse("handling=lenient") == `handling=lenient`)
    assert(Preference("handling", "lenient") == `handling=lenient`)
    assert(Preference("handling", "lenient", Map("stop" -> None)) == `handling=lenient`)

    assert(Preference.parse("Return=representation") == `return=representation`)
    assert(Preference("return", "representation") == `return=representation`)
    assert(Preference("return", "representation", Map("stop" -> None)) == `return=representation`)

    assert(Preference.parse("Return=minimal") == `return=minimal`)
    assert(Preference("return", "minimal") == `return=minimal`)
    assert(Preference("return", "minimal", Map("stop" -> None)) == `return=minimal`)

    assert(Preference.parse("respond-async=value") == `respond-async`)
    assert(Preference("respond-async") == `respond-async`)
    assert(Preference("respond-async", Map("stop" -> None)) == `respond-async`)
  }

  it should "not be created with invalid value" in {
    assertThrows[IllegalArgumentException](Preference.parse("pref value"))
    assertThrows[IllegalArgumentException](Preference.parse("pref=value param1"))
    assertThrows[IllegalArgumentException](Preference.parse("pref=value; param1 value1"))
  }
