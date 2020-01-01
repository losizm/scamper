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

/** Defines frame for WebSocket message. */
trait MaskingKey {
  /** Gets key value. */
  def value: Int

  /**
   * Applies key to supplied data.
   *
   * @param data bytes to which key is applied
   *
   * @return modified data
   */
  def apply(data: Array[Byte]): Array[Byte]
}

/** Provides factory methods for `MaskingKey`. */
object MaskingKey {
  private val random = new java.security.SecureRandom()

  /**
   * Creates masking key with supplied value if nonzero.
   *
   * @return `Some` masking key if `value` is nonzero; `None` otherwise
   */
  def get(value: Int): Option[MaskingKey] =
    (value == 0) match {
      case true  => None
      case fale  => Some(new MaskingKeyImpl(value))
    }
      
  /**
   * Creates masking key with supplied value.
   *
   * @throws IllegalArgumentException if `value` is zero
   */
  def apply(value: Int): MaskingKey = {
    if (value == 0)
      throw new IllegalArgumentException("value cannot be 0")
    new MaskingKeyImpl(value)
  }

  /** Creates masking key with randomly generated value. */
  def apply(): MaskingKey = {
    var value = 0
    while (value == 0)
      value = random.nextInt()
    new MaskingKeyImpl(value)
  }
}

private case class MaskingKeyImpl(value: Int) extends MaskingKey {
  override lazy val toString: String = s"MaskingKey($value)"

  def apply(data: Array[Byte]): Array[Byte] = {
    val key = Array[Byte](
      { (value & 0xff000000) >> 24 }.toByte,
      { (value & 0x00ff0000) >> 16 }.toByte,
      { (value & 0x0000ff00) >>  8 }.toByte,
      { (value & 0x000000ff) >>  0 }.toByte
    )

    for (i <- 0 until data.size)
      data(i) = { data(i) ^ key(i % 4) }.toByte
    data
  }
}
