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

class LinkValueSpec extends FlatSpec {
  "LinkValue" should "be created" in {
    var link = LinkValue("/assets/icon.png")
    assert(link.ref == "/assets/icon.png")
    assert(link.params.isEmpty)
    assert(link.toString == "</assets/icon.png>")
    assert(link == LinkValue("/assets/icon.png"))

    link = LinkValue("/assets/large-icon.png", "size" -> Some("64x64"))
    assert(link.ref == "/assets/large-icon.png")
    assert(link.params("size") == Some("64x64"))
    assert(link.toString == "</assets/large-icon.png>; size=64x64")
    assert(link == LinkValue("/assets/large-icon.png", "size" -> Some("64x64")))
  }

  it should "be parsed" in {
    assert(LinkValue.parse("</assets/icon.png>") == LinkValue("/assets/icon.png"))
    assert(LinkValue.parse("</assets/large-icon.png>;size=64x64") == LinkValue("/assets/large-icon.png", "size" -> Some("64x64")))
    assert {
      LinkValue.parseAll("</assets/icon.png>,</assets/large-icon.png>;size=64x64") ==
        Seq(LinkValue("/assets/icon.png"),LinkValue("/assets/large-icon.png", "size" -> Some("64x64")))
    }
  }

  it should "be destructured" in {
    LinkValue.parse("</assets/icon.png>") match {
      case LinkValue(ref, params) => assert(ref == "/assets/icon.png" && params.isEmpty)
    }

    LinkValue.parse("</assets/large-icon.png>;size=64x64") match {
      case LinkValue(ref, params) =>
        assert(ref == "/assets/large-icon.png")
        assert(params("size") == Some("64x64"))
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](LinkValue.parse("/assets/icon.png"))
    assertThrows[IllegalArgumentException](LinkValue.parse("/assets/icon.png>; size=64x64"))
  }
}