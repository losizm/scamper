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

class EntityTagSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "EntityTag" should "be created" in {
    var tag = EntityTag.parse("\"abc\"")
    assert(tag.opaque == "\"abc\"")
    assert(!tag.weak)
    assert(tag.toString == "\"abc\"")
    assert(tag == EntityTag("\"abc\"", false))

    tag = EntityTag.parse("W/\"xyz\"")
    assert(tag.opaque == "\"xyz\"")
    assert(tag.weak)
    assert(tag.toString == "W/\"xyz\"")
    assert(tag == EntityTag("xyz", true))
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](EntityTag.parse("w/\"abc\""))
    assertThrows[IllegalArgumentException](EntityTag.parse("abc"))
    assertThrows[IllegalArgumentException](EntityTag.parse("\"abc\"xyz\""))
  }
