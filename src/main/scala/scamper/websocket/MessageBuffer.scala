/*
 * Copyright 2020 Carlos Conyers
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

import java.io.ByteArrayOutputStream
import java.util.zip.{ Inflater, InflaterOutputStream }

private trait MessageBuffer {
  def add(data: Array[Byte]): Unit
  def size: Int
  def get: Array[Byte] 
}

private class IdentityMessageBuffer extends MessageBuffer {
  private val buffer = new ByteArrayOutputStream()

  def add(data: Array[Byte]): Unit = buffer.write(data)
  def size: Int = buffer.size
  def get: Array[Byte] = buffer.toByteArray
}

private class InflaterMessageBuffer extends MessageBuffer {
  private val buffer = new ByteArrayOutputStream()
  private val decompressor = new InflaterOutputStream(buffer, new Inflater(true))

  def add(data: Array[Byte]): Unit = {
    decompressor.write(data)
    decompressor.flush()
  }

  def size: Int = buffer.size

  def get: Array[Byte] = {
    decompressor.finish()
    decompressor.flush()
    decompressor.close()

    buffer.toByteArray
  }
}
