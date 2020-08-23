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
package scamper

import java.io.InputStream
import java.util.zip.{ DeflaterInputStream, GZIPOutputStream }

import scala.concurrent.ExecutionContext

private object Compressor {
  def gzip(in: InputStream, bufferSize: Int = 8192)(implicit ec: ExecutionContext): InputStream =
    new WriterInputStream(out => write(in, new GZIPOutputStream(out), bufferSize))

  def deflate(in: InputStream, bufferSize: Int = 8192): InputStream =
    new DeflaterInputStream(in)

  private def write(in: InputStream, out: GZIPOutputStream, bufferSize: Int): Unit = {
    val buffer = new Array[Byte](bufferSize)
    var length = 0

    while ({ length = in.read(buffer); length != -1 })
      out.write(buffer, 0, length)
    out.finish()
    out.flush()
  }
}

