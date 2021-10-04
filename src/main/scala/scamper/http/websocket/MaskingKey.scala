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
package websocket

/** Defines masking key for payload data. */
trait MaskingKey:
  /** Gets key value. */
  def value: Int

  /**
   * Applies key to supplied data.
   *
   * @param data bytes to which key is applied
   *
   * @return modified data
   *
   * @note Equivalent to: `apply(data, data.length, 0)`
   */
  def apply(data: Array[Byte]): Array[Byte] =
    apply(data, data.length, 0)

  /**
   * Applies key to `length` bytes of `data`, assuming first byte corresponds to
   * `position` in payload.
   *
   * @param data bytes to which key is applied
   * @param length number of bytes to which key is applied
   * @param position offset into payload to which first byte of data corresponds
   *
   * @return modified data
   */
  def apply(data: Array[Byte], length: Int, position: Long): Array[Byte]

/** Provides factory for `MaskingKey`. */
object MaskingKey:
  private val random = java.security.SecureRandom()

  /**
   * Creates masking key with supplied value if nonzero.
   *
   * @return `Some` masking key if `value` is nonzero; `None` otherwise
   */
  def get(value: Int): Option[MaskingKey] =
    value == 0 match
      case true  => None
      case fale  => Some(MaskingKeyImpl(value))

  /**
   * Creates masking key with supplied value.
   *
   * @throws java.lang.IllegalArgumentException if `value` is zero
   */
  def apply(value: Int): MaskingKey =
    if value == 0 then
      throw IllegalArgumentException("value is 0")
    MaskingKeyImpl(value)

  /** Creates masking key with randomly generated value. */
  def apply(): MaskingKey =
    var value = 0
    while value == 0 do
      value = random.nextInt()
    MaskingKeyImpl(value)

private case class MaskingKeyImpl(value: Int) extends MaskingKey:
  private val key = Array(
    ((value & 0xff000000) >> 24).toByte,
    ((value & 0x00ff0000) >> 16).toByte,
    ((value & 0x0000ff00) >>  8).toByte,
    ((value & 0x000000ff) >>  0).toByte
  )

  override lazy val toString: String = f"MaskingKey(0x$value%08x)"

  def apply(data: Array[Byte], length: Int, position: Long): Array[Byte] =
    val offset = (position % 4).toInt
    for i <- 0 until length do
      data(i) = (data(i) ^ key((offset + i) % 4)).toByte
    data
