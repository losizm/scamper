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

import java.io.InputStream
import java.util.zip.{ DeflaterInputStream, GZIPOutputStream }

import scala.concurrent.ExecutionContext

private object Compressor:
  def gzip(in: InputStream, bufferSize: Int = 8192)(using ec: ExecutionContext): InputStream =
    WriterInputStream(out => write(in, GZIPOutputStream(out), bufferSize))

  def deflate(in: InputStream, bufferSize: Int = 8192): InputStream =
    DeflaterInputStream(in)

  private def write(in: InputStream, out: GZIPOutputStream, bufferSize: Int): Unit =
    out.write(in, new Array[Byte](bufferSize))
    out.finish()
    out.flush()
