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

import java.io.{ FilterInputStream, InputStream }

private class BoundedInputStream(in: InputStream, limit: Long, capacity: Long) extends FilterInputStream(in) {
  def this(in: InputStream, limit: Long) = this(in, limit, limit)

  private var position: Long = 0

  override def read(): Int =
    if (position >= capacity) -1
    else
      in.read() match {
        case -1   => -1
        case byte => position += 1; byte
      }

  override def read(buffer: Array[Byte], offset: Int, length: Int): Int =
    if (position >= capacity) -1
    else
      in.read(buffer, offset, length.min(maxRead)) match {
        case -1    => -1
        case count =>
          position += count
          if (position > limit) throw ReadLimitExceeded(limit)
          count
      }

  private def maxRead: Int =
    (capacity - position).min(Int.MaxValue).toInt
}
