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

import java.util.Base64.{ getDecoder, getEncoder }

private object Base64:
  private val encoder = getEncoder()
  private val decoder = getDecoder()

  def encode(value: Array[Byte]): Array[Byte] =
    encoder.encode(value)

  def encode(value: String): Array[Byte] =
    encoder.encode(value.getBytes("UTF-8"))

  def encodeToString(value: Array[Byte]): String =
    encoder.encodeToString(value)

  def encodeToString(value: String): String =
    encodeToString(value.getBytes("UTF-8"))

  def decode(value: Array[Byte]): Array[Byte] =
    decoder.decode(value)

  def decode(value: String): Array[Byte] =
    decoder.decode(value)

  def decodeToString(value: Array[Byte]): String =
    String(decode(value), "UTF-8")

  def decodeToString(value: String): String =
    String(decode(value), "UTF-8")
