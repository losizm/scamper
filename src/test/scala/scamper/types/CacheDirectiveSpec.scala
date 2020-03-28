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
package scamper.types

import CacheDirectives._

class CacheDirectiveSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "CacheDirective" should "be created" in {
    var directive = CacheDirective("No-Cache")
    assert(directive.name == "no-cache")
    assert(!directive.value.isDefined)
    assert(directive.toString == "no-cache")
    assert(directive == `no-cache`)

    directive = CacheDirective("MAX-AGE", Some("6000"))
    assert(directive.name == "max-age")
    assert(directive.value.contains("6000"))
    assert(directive.toString == "max-age=6000")
    assert(directive == `max-age`(6000))

    val maxAge = `max-age`(3000)
    assert(maxAge.name == "max-age")
    assert(maxAge.value.contains("3000"))
    assert(maxAge.seconds == 3000)
    assert(maxAge.toString == "max-age=3000")
    assert(maxAge == CacheDirective("max-age", Some("3000")))

    val ext = CacheDirective("community", Some("In Library"))
    assert(ext.name == "community")
    assert(ext.value.contains("In Library"))
    assert(ext.toString == "community=\"In Library\"")
    assert(ext == CacheDirective("Community", Some("In Library")))

    assert(CacheDirective("Immutable") == `immutable`)
    assert(CacheDirective("MUST-REVALIDATE") == `must-revalidate`)
    assert(CacheDirective("no-cache") == `no-cache`)
    assert(CacheDirective("NO-store") == `no-store`)
    assert(CacheDirective("no-TRANSFORM") == `no-transform`)
    assert(CacheDirective("only-IF-cached") == `only-if-cached`)
    assert(CacheDirective("PrIvaTe") == `private`)
    assert(CacheDirective("Proxy-revalidate") == `proxy-revalidate`)
    assert(CacheDirective("public") == `public`)
  }

  it should "be parsed" in {
    assert(CacheDirective.parse("no-store") == `no-store`)
    assert(CacheDirective.parse("min-fresh = 90") == `min-fresh`(90))
    assert(CacheDirective.parseAll("min-fresh = 90") == Seq(`min-fresh`(90)))
    assert(CacheDirective.parseAll("no-store, max-age=120") == Seq(`no-store`, `max-age`(120)))
  }

  it should "be destructured" in {
    val sMaxage = `s-maxage`(60)

    sMaxage match {
      case CacheDirective(name, Some(value)) =>
        assert(name == "s-maxage")
        assert(value == "60")
    }

    sMaxage match {
      case `s-maxage`(secs) => assert(secs == 60)
    }

    val mustRevalidate = CacheDirective("Must-Revalidate")

    mustRevalidate match {
      case CacheDirective(name, value) =>
        assert(name == "must-revalidate")
        assert(!value.isDefined)
    }

    mustRevalidate match {
      case `must-revalidate` =>
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](CacheDirective("no-cache /"))
    assertThrows[IllegalArgumentException](CacheDirective("no-cache ="))
  }
}
