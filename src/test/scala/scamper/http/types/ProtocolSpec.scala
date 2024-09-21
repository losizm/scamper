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

class ProtocolSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "Protocol" should "be created" in {
    var protocol = Protocol.parse("HTTP/2.0")
    assert(protocol.name == "HTTP")
    assert(protocol.version.contains("2.0"))
    assert(protocol.toString == "HTTP/2.0")
    assert(protocol == Protocol("HTTP", Some("2.0")))

    protocol = Protocol.parse("SHTTP/1.3")
    assert(protocol.name == "SHTTP")
    assert(protocol.version.contains("1.3"))
    assert(protocol.toString == "SHTTP/1.3")
    assert(protocol == Protocol("SHTTP", Some("1.3")))

    protocol = Protocol.parse("IRC")
    assert(protocol.name == "IRC")
    assert(protocol.version.isEmpty)
    assert(protocol.toString == "IRC")
    assert(protocol == Protocol("IRC", None))
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Protocol.parse("HTTP / 2.0"))
    assertThrows[IllegalArgumentException](Protocol.parse("HTTP/"))
  }
