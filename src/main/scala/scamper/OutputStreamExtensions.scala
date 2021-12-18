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

import java.io.OutputStream

private implicit class OutputStreamExtensions(out: OutputStream) extends AnyVal:
  def writeLine(text: String): Unit =
    out.write(text.getBytes("UTF-8"))
    out.write(Array[Byte](13, 10))

  def writeLine(): Unit =
    out.write(Array[Byte](13, 10))
