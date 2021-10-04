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

class LinkTypeSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "LinkType" should "be created" in {
    var link = LinkType(Uri("/assets/icon.png"))
    assert(link.ref.toString == "/assets/icon.png")
    assert(link.params.isEmpty)
    assert(link.toString == "</assets/icon.png>")
    assert(link == LinkType(Uri("/assets/icon.png")))

    link = LinkType(Uri("/assets/large-icon.png"), "size" -> Some("64x64"))
    assert(link.ref.toString == "/assets/large-icon.png")
    assert(link.params("size") == Some("64x64"))
    assert(link.toString == "</assets/large-icon.png>; size=64x64")
    assert(link == LinkType(Uri("/assets/large-icon.png"), "size" -> Some("64x64")))
  }

  it should "be parsed" in {
    assert(LinkType.parse("</assets/icon.png>") == LinkType(Uri("/assets/icon.png")))
    assert(LinkType.parse("</assets/large-icon.png>;size=64x64") == LinkType(Uri("/assets/large-icon.png"), "size" -> Some("64x64")))
    assert {
      LinkType.parseAll("</assets/icon.png>,</assets/large-icon.png>;size=64x64") ==
        Seq(LinkType(Uri("/assets/icon.png")),LinkType(Uri("/assets/large-icon.png"), "size" -> Some("64x64")))
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](LinkType.parse("/assets/icon.png"))
    assertThrows[IllegalArgumentException](LinkType.parse("/assets/icon.png>; size=64x64"))
  }
