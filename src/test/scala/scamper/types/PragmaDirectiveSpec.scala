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

import PragmaDirectives.*

class PragmaDirectiveSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "PragmaDirective" should "be created" in {
    val directive = PragmaDirective("No-Cache")
    assert(directive.name == "no-cache")
    assert(!directive.value.isDefined)
    assert(directive.toString == "no-cache")
    assert(directive == `no-cache`)

    val ext = PragmaDirective("community", Some("In Library"))
    assert(ext.name == "community")
    assert(ext.value.contains("In Library"))
    assert(ext.toString == "community=\"In Library\"")
    assert(ext == PragmaDirective("Community", Some("In Library")))
  }

  it should "be parsed" in {
    assert(PragmaDirective.parse("no-cache") == `no-cache`)
    assert(PragmaDirective.parse("community = \"home\"") == PragmaDirective("Community", Some("home")))
    assert(PragmaDirective.parseAll("no-cache, community = \"home\"") == Seq(`no-cache`, PragmaDirective("Community", Some("home"))))
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](PragmaDirective.parse("no/cache"))
    assertThrows[IllegalArgumentException](PragmaDirective.parse("no-cache ="))
  }
