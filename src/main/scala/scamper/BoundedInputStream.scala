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
package scamper

import java.io.{ FilterInputStream, InputStream, IOException }

/**
 * Indicates attempt to read beyond an `InputStream`'s established limit.
 *
 * `ReadLimitExceeded` is a complement to `EntityTooLarge`. Whereas
 * `ReadLimitExceeded` applies to the raw bytes of an input stream,
 * `EntityTooLarge` pertains to the entity itself, potentially subjected to
 * decompression.
 *
 * @see [[EntityTooLarge]]
 */
case class ReadLimitExceeded(limit: Long) extends IOException(s"Cannot read beyond $limit byte(s)")

private class BoundedInputStream(in: InputStream, limit: Long, capacity: Long) extends FilterInputStream(in) {
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

