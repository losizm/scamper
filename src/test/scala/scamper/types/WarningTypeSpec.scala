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
import scamper.DateValue

class WarningTypeSpec extends FlatSpec {
  val date = DateValue.parse("Sat, 25 Aug 2012 23:34:45 GMT")

  "WarningType" should "be created" in {
    var warning = WarningType(110, "localhost:8000", "Response is Stale", Some(date))
    assert(warning.code == 110)
    assert(warning.agent == "localhost:8000")
    assert(warning.text == "Response is Stale")
    assert(warning.date.contains(date))
    assert(warning.toString == "110 localhost:8000 \"Response is Stale\" \"Sat, 25 Aug 2012 23:34:45 GMT\"")

    warning = WarningType(299, "-", "Miscellaneous Persistent Warning")
    assert(warning.code == 299)
    assert(warning.agent == "-")
    assert(warning.text == "Miscellaneous Persistent Warning")
    assert(warning.date == None)
    assert(warning.toString == "299 - \"Miscellaneous Persistent Warning\"")
  }

  it should "be parsed" in {
    val warn1 = WarningType.parse("110 localhost:8000 \"Response is Stale\" \"Sat, 25 Aug 2012 23:34:45 GMT\"")
    assert(warn1.code == 110)
    assert(warn1.agent == "localhost:8000")
    assert(warn1.text == "Response is Stale")
    assert(warn1.date.contains(date))
    assert(warn1.toString == "110 localhost:8000 \"Response is Stale\" \"Sat, 25 Aug 2012 23:34:45 GMT\"")

    val warn2 = WarningType.parse("299 - \"Miscellaneous Persistent Warning\"")
    assert(warn2.code == 299)
    assert(warn2.agent == "-")
    assert(warn2.text == "Miscellaneous Persistent Warning")
    assert(warn2.date == None)
    assert(warn2.toString == "299 - \"Miscellaneous Persistent Warning\"")

    assert(WarningType.parseAll(warn1 + ", " + warn2) == Seq(warn1, warn2))
  }

  it should "be destructured" in {
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](WarningType.parse("110 localhost:8000"))
    assertThrows[IllegalArgumentException](WarningType.parse("110 localhost:8000 Response is Stale"))
    assertThrows[IllegalArgumentException](WarningType.parse("100 localhost:8000 \"Response is Stale\" Sat, 25 Aug 2012 23:34:45 GMT"))
  }
}
