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
package http
package multipart

import java.io.OutputStream

private case class MultipartEntity(multipart: Multipart, boundary: String) extends Entity:
  val knownSize = None
  lazy val data = WriterInputStream(writeMultipart)(using Auxiliary.executor)

  private def writeMultipart(out: OutputStream): Unit =
    val start = "--" + boundary
    val end   = "--" + boundary + "--"

    multipart.parts.foreach { part =>
      out.writeLine(start)
      out.writeLine("Content-Disposition: " + part.contentDisposition.toString)

      if !part.contentType.isText || part.contentType.subtypeName != "plain" || part.contentType.params.nonEmpty then
        out.writeLine("Content-Type: " + part.contentType.toString)
      out.writeLine()

      part match
        case _: StringPart =>
          out.writeLine(part.getString())

        case _: ByteArrayPart =>
          out.write(part.getBytes())
          out.writeLine()

        case _: FilePart =>
          part.withInputStream { in =>
            out.write(in, new Array[Byte](8192))
            out.writeLine()
          }
    }

    out.writeLine(end)
    out.flush()
