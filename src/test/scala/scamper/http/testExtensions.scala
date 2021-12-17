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

import java.io.InputStream
import scala.collection.mutable.ArrayBuffer

extension (in: InputStream)
  def readBytes(bufferSize: Int = 8192): Array[Byte] =
    val bytes = ArrayBuffer[Byte]()
    val buffer = new Array[Byte](bufferSize.max(1024))
    var len = 0

    while { len = in.read(buffer); len != -1 } do
      bytes ++= buffer.take(len)

    bytes.toArray

extension (body: Entity)
  def toByteArray: Array[Byte] =
    try body.data.readBytes() finally body.data.close()
