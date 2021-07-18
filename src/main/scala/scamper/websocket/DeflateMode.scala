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

import java.io.InputStream

private[scamper] sealed trait DeflateMode:
  def compressed: Boolean
  def continuation: Boolean
  def prepare(data: InputStream): InputStream
  def apply(payload: Array[Byte], length: Int): (Array[Byte], Int)

private[scamper] object DeflateMode:
  object None extends DeflateMode:
    val compressed = false
    val continuation = false
    def prepare(data: InputStream) = data
    def apply(payload: Array[Byte], length: Int) = (payload, length)

  object Message extends DeflateMode:
    val compressed = true
    val continuation = false
    def prepare(data: InputStream) = WebSocketDeflate.compress(data)
    def apply(payload: Array[Byte], length: Int) = (payload, length)

  object Frame extends DeflateMode:
    val compressed = true
    val continuation = true
    def prepare(data: InputStream) = data
    def apply(payload: Array[Byte], length: Int) =
      val deflated = WebSocketDeflate.compress(payload, 0, length)
      (deflated, deflated.length)
