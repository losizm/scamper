/*
 * Copyright 2019 Carlos Conyers
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

import ViaType.{ parse => ParseVia }

class ViaTypeSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "ViaType" should "be created" in {
    var via = ParseVia("1.1 www.hub.com:7777 (  Hub/0.1b  )")
    assert(via.protocol == Protocol.parse("HTTP/1.1"))
    assert(via.by == "www.hub.com:7777")
    assert(via.comment.contains("Hub/0.1b"))
    assert(via.toString == "1.1 www.hub.com:7777 (Hub/0.1b)")
    assert(via == ViaType(Protocol.parse("HTTP/1.1"), "www.hub.com:7777", Some("Hub/0.1b")))

    via = ParseVia("SHTTP/1.3 secret-gateway")
    assert(via.protocol == Protocol.parse("SHTTP/1.3"))
    assert(via.by == "secret-gateway")
    assert(!via.comment.isDefined)
    assert(via.toString == "SHTTP/1.3 secret-gateway")
    assert(via == ViaType(Protocol.parse("SHTTP/1.3"), "secret-gateway"))

    via = ParseVia("IRC/6.9 irc.net:99 ()")
    assert(via.protocol == Protocol.parse("IRC/6.9"))
    assert(via.by == "irc.net:99")
    assert(via.comment.contains(""))
    assert(via.toString == "IRC/6.9 irc.net:99 ()")
    assert(via == ViaType(Protocol.parse("IRC/6.9"), "irc.net:99", Some("   ")))
  }

  it should "be destructured" in {
    ParseVia("1.1 www.hub.com:7777 (Hub/0.1b)") match {
      case ViaType(protocol, by, Some(comment)) =>
        assert(protocol == Protocol.parse("HTTP/1.1"))
        assert(by == "www.hub.com:7777")
        assert(comment == "Hub/0.1b")
    }

    ParseVia("SHTTP/1.3 secret-gateway") match {
      case ViaType(protocol, by, None) =>
        assert(protocol == Protocol.parse("SHTTP/1.3"))
        assert(by == "secret-gateway")
    }

    ParseVia("IRC/6.9 irc.net:99 ()") match {
      case ViaType(protocol, by, Some(comment)) =>
        assert(protocol == Protocol.parse("IRC/6.9"))
        assert(by == "irc.net:99")
        assert(comment.isEmpty)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](ParseVia("HTTP/1.1"))
    assertThrows[IllegalArgumentException](ParseVia("HTTP/1.1 (Hub/0.1b)"))
  }
}
