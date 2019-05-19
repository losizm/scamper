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

import java.io.InputStream

private object EmptyInputStream extends InputStream {
  override def available(): Int = 0
  override def read(): Int = -1
  override def read(buffer: Array[Byte]): Int = -1
  override def read(buffer: Array[Byte], offset: Int, length: Int): Int = -1
  override def skip(length: Long): Long = 0
  override def close(): Unit = ()
  override def markSupported(): Boolean = true
  override def mark(limit: Int): Unit = ()
  override def reset(): Unit = ()
}

