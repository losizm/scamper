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
package scamper.websocket

/**
 * Defines opcode for WebSocket frame.
 *
 * @see [[Opcode.Registry]]
 */
trait Opcode {
  /** Gets value. */
  def value: Int

  /** Gets meaning. */
  def meaning: String

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
    val Continuation: Opcode = OpcodeImpl(0, "Continuation")

    /** 1 &ndash; Text Frame */
    val Text: Opcode = OpcodeImpl(1, "Text")

    /** 2 &ndash; Binary Frame */
    val Binary: Opcode = OpcodeImpl(2, "Binary")

    /** 8 &ndash; Close Frame */
    val Close: Opcode = OpcodeImpl(8, "Close")

    /** 9 &ndash; Ping Frame */
    val Ping: Opcode = OpcodeImpl(9, "Ping")

    /** 10 &ndash; Pong Frame */
    val Pong: Opcode = OpcodeImpl(10, "Pong")
  }

  /** Gets opcode for given value, if registered. */
  def get(value: Int): Option[Opcode] =
    try Some(apply(value))
    catch {
      case _: NoSuchElementException => None
    }

  /**
   * Gets registered opcode for given value.
   *
   * @throws java.util.NoSuchElementException if value not registered
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

private case class OpcodeImpl(value: Int, meaning: String) extends Opcode {
  override lazy val toString: String = s"$value ($meaning)"
}
