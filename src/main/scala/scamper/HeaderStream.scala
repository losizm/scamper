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

import scala.collection.mutable.ArrayBuffer

import Auxiliary.InputStreamType

private object HeaderStream {
  def getHeaders(in: InputStream, buffer: Array[Byte]): Seq[Header] = {
    val headers = new ArrayBuffer[Header]
    var line = ""

    while ({ line = in.getLine(buffer); line != "" })
      line.matches("[ \t]+.*") match {
        case true =>
          if (headers.isEmpty) throw new HttpException("Cannot parse headers")
          val last = headers.last
          headers.update(headers.length - 1, Header(last.name, last.value + " " + line.trim()))
        case false =>
          headers += Header(line)
      }

    headers.toSeq
  }
}

