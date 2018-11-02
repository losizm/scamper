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

class MediaRangeSpec extends FlatSpec {
  "MediaRange" should "be created without parameters" in {
    var range = MediaRange.parse("text/html; q=1.0")
    assert(range.mainType == "text")
    assert(range.subtype == "html")
    assert(range.weight == 1f)
    assert(range.params.isEmpty)
    assert(range.isText)
    assert(range.toString == "text/html")

    range = MediaRange.parse("text/html; q=0.7")
    assert(range.mainType == "text")
    assert(range.subtype == "html")
    assert(range.weight == 0.7f)
    assert(range.params.isEmpty)
    assert(range.isText)
    assert(range.toString == "text/html; q=0.7")

    range = MediaRange.parse("*/html; q=7.")
    assert(range.mainType == "*")
    assert(range.subtype == "html")
    assert(range.weight == 1f)
    assert(range.params.isEmpty)
    assert(range.isWildcard)
    assert(range.toString == "*/html")

    range = MediaRange("text", "html", -7f)
    assert(range.mainType == "text")
    assert(range.subtype == "html")
    assert(range.weight == 0f)
    assert(range.params.isEmpty)
    assert(range.isText)
    assert(range.toString == "text/html; q=0.0")
  }

  it should "be created with parameters" in {
    var range = MediaRange.parse("text/html; charset=iso-8859-1")
    assert(range.mainType == "text")
    assert(range.subtype == "html")
    assert(range.params("charset") == "iso-8859-1")
    assert(range.isText)
    assert(range.toString == "text/html; charset=iso-8859-1")

    range = MediaRange("text", "html", 0.7f, Map("charset" -> "utf-8", "not-a-charset" -> "iso 8859 1"))
    assert(range.mainType == "text")
    assert(range.subtype == "html")
    assert(range.params("charset") == "utf-8")
    assert(range.params("not-a-charset") == "iso 8859 1")
    assert(range.isText)
    assert(range.toString == "text/html; q=0.7; charset=utf-8; not-a-charset=\"iso 8859 1\"")
  }

  it should "match MediaType" in {
    var range = MediaRange.parse("text/html")
    assert(range.matches(MediaType.parse("text/html")))
    assert(range.matches(MediaType.parse("text/html; charset=ascii")))

    range = MediaRange.parse("*/html")
    assert(range.matches(MediaType.parse("text/html")))
    assert(range.matches(MediaType.parse("text/html; charset=ascii")))

    range = MediaRange.parse("*/*; q=0.1; charset=ascii")
    assert(range.matches(MediaType.parse("text/html; charset=ascii; level=1")))
    assert(range.matches(MediaType.parse("text/html; Charset=ASCII")))
  }

  it should "not match MediaType" in {
    var range = MediaRange.parse("text/html")
    assert(!range.matches(MediaType.parse("text/plain")))
    assert(!range.matches(MediaType.parse("example/html; charset=ascii")))

    range = MediaRange.parse("*/html")
    assert(!range.matches(MediaType.parse("text/plain")))

    range = MediaRange.parse("*/*; q=0.1; charset=ascii")
    assert(!range.matches(MediaType.parse("text/html; charset=utf-8; level=1")))
    assert(!range.matches(MediaType.parse("text/html; charset=utf-8")))
  }

  it should "be destructured" in {
    val range = MediaRange.parse("text/html; charset=iso-8859-1")

    range match {
      case MediaRange(mainType, subtype, weight, params) =>
        assert(mainType == range.mainType)
        assert(subtype == range.subtype)
        assert(weight == range.weight)
        assert(params == range.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](MediaRange.parse("(text)/html"))
    assertThrows[IllegalArgumentException](MediaRange.parse("text/(html)"))
    assertThrows[IllegalArgumentException](MediaRange.parse("text/html; charset"))
    assertThrows[IllegalArgumentException](MediaRange.parse("text/html; charset=iso 8859 1"))
  }
}