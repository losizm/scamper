/*
 * Copyright 2018 Carlos Conyers
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

import java.io.{ FilterInputStream, InputStream }

private class BoundInputStream(in: InputStream, maxLength: Long) extends FilterInputStream(in) {
  private var position: Long = 0

  override def read(): Int =
    if (position >= maxLength) -1
    else
      in.read() match {
        case -1   => -1
        case byte => position += 1; byte
      }

  override def read(buffer: Array[Byte], offset: Int, length: Int): Int =
    if (position >= maxLength) -1
    else
      in.read(buffer, offset, length.min(maxRead)) match {
        case -1  => -1
        case len => position += len; len
      }

  private def maxRead: Int =
    (maxLength - position).min(Int.MaxValue).toInt
}

