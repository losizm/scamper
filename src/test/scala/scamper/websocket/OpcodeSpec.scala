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
package scamper.websocket

import Opcode.Registry.*

class OpcodeSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "check registered opcodes" in {
    assert(Opcode(0) == Continuation)
    assert(Opcode.get(0).contains(Continuation))
    assert(Continuation.value == 0)
    assert(Continuation.meaning == "Continuation")
    assert(!Continuation.isControl)

    assert(Opcode(1) == Text)
    assert(Opcode.get(1).contains(Text))
    assert(Text.value == 1)
    assert(Text.meaning == "Text")
    assert(!Text.isControl)

    assert(Opcode(2) == Binary)
    assert(Opcode.get(2).contains(Binary))
    assert(Binary.value == 2)
    assert(Binary.meaning == "Binary")
    assert(!Binary.isControl)

    assert(Opcode(8) == Close)
    assert(Opcode.get(8).contains(Close))
    assert(Close.value == 8)
    assert(Close.meaning == "Close")
    assert(Close.isControl)

    assert(Opcode(9) == Ping)
    assert(Opcode.get(9).contains(Ping))
    assert(Ping.value == 9)
    assert(Ping.meaning == "Ping")
    assert(Ping.isControl)

    assert(Opcode(10) == Pong)
    assert(Opcode.get(10).contains(Pong))
    assert(Pong.value == 10)
    assert(Pong.meaning == "Pong")
    assert(Pong.isControl)
  }

  it should "not create invalid opcodes" in {
    assertThrows[NoSuchElementException](Opcode(-1))
    assertThrows[NoSuchElementException](Opcode(3))
    assertThrows[NoSuchElementException](Opcode(4))
    assertThrows[NoSuchElementException](Opcode(5))
    assertThrows[NoSuchElementException](Opcode(6))
    assertThrows[NoSuchElementException](Opcode(7))
    assertThrows[NoSuchElementException](Opcode(11))

    assert(Opcode.get(-1).isEmpty)
    assert(Opcode.get(3).isEmpty)
    assert(Opcode.get(4).isEmpty)
    assert(Opcode.get(5).isEmpty)
    assert(Opcode.get(6).isEmpty)
    assert(Opcode.get(7).isEmpty)
    assert(Opcode.get(11).isEmpty)
  }
