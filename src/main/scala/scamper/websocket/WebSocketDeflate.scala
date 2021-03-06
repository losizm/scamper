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

import java.io._
import java.util.zip._

private object WebSocketDeflate {
  def compress(message: InputStream): InputStream =
    new SequenceInputStream(
      new DeflaterInputStream(message, new Deflater(Deflater.DEFAULT_COMPRESSION, true)),
      new ByteArrayInputStream(new Array[Byte](1))
    )

  def compress(message: Array[Byte]): Array[Byte] =
    compress(message, 0, message.length)

  def compress(message: Array[Byte], offset: Int, length: Int): Array[Byte] = {
    val buffer = new ByteArrayOutputStream()
    val compressor = new DeflaterOutputStream(buffer, new Deflater(Deflater.DEFAULT_COMPRESSION, true))

    compressor.write(message, offset, length)
    compressor.finish()
    compressor.flush()
    compressor.close()

    buffer.write(0)
    buffer.toByteArray()
  }

  def decompress(message: InputStream): InputStream =
    new InflaterInputStream(message, new Inflater(true))

  def decompress(message: Array[Byte]): Array[Byte] =
    decompress(message, 0, message.length)

  def decompress(message: Array[Byte], offset: Int, length: Int): Array[Byte] = {
    val buffer = new ByteArrayOutputStream()
    val decompressor = new InflaterOutputStream(buffer, new Inflater(true))

    decompressor.write(message, offset, length)
    decompressor.finish()
    decompressor.flush()
    decompressor.close()

    buffer.toByteArray()
  }
}
