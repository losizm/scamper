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
package scamper.websocket

/**
 * Defines opcode for WebSocket frame.
 *
 * @see [[Opcode.Registry]]
 */
trait Opcode {
  /** Gets value. */
  def value: Int

  /** Tests for control frame opcode. */
  def isControl: Boolean = value >= 8
}

/**
 * Provides factory methods and registry for `Opcode`.
 *
 * @see [[Opcode.Registry]]
 */
object Opcode {
  /** Contains registered WebSocket opcodes. */
  object Registry {
    /** 0 &ndash; Continuation Frame */
    val Continuation: Opcode = OpcodeImpl(0)

    /** 1 &ndash; Text Frame */
    val Text: Opcode = OpcodeImpl(1)

    /** 2 &ndash; Binary Frame */
    val Binary: Opcode = OpcodeImpl(2)

    /** 8 &ndash; Close Frame */
    val Close: Opcode = OpcodeImpl(8)

    /** 9 &ndash; Ping Frame */
    val Ping: Opcode = OpcodeImpl(9)

    /** 10 &ndash; Pong Frame */
    val Pong: Opcode = OpcodeImpl(10)
  }

  /** Gets `Opcode` for given value, if registered. */
  def get(value: Int): Option[Opcode] =
    try Some(apply(value))
    catch {
      case _: NoSuchElementException => None
    }

  /**
   * Gets registered `Opcode` for given value.
   *
   * @throws NoSuchElementException if value not registered
   */
  def apply(value: Int): Opcode =
    value match {
      case  0 => Registry.Continuation
      case  1 => Registry.Text
      case  2 => Registry.Binary
      case  8 => Registry.Close
      case  9 => Registry.Ping
      case 10 => Registry.Pong
      case _  => throw new NoSuchElementException()
    }

  /** Destructures supplied opcode to its `value`. */
  def unapply(code: Opcode): Option[Int] =
    Some(code.value)
}

private case class OpcodeImpl(value: Int) extends Opcode {
  override lazy val toString: String = s"Opcode($value)"
}
