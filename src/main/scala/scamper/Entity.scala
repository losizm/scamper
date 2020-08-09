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

import java.io.{ ByteArrayInputStream, File, FileInputStream, InputStream, OutputStream }
import java.nio.file.Path

import Auxiliary.{ FileType, OutputStreamType }
import Validate.notNull

/** Representation of message body. */
trait Entity {
  /** Gets length in bytes if known. */
  def getLength: Option[Long]

  /** Tests for empty (if known). */
  def isKnownEmpty: Boolean =
    getLength.contains(0)

  /** Gets input stream to entity. */
  def getInputStream: InputStream

  /**
   * Gets input stream and passes it to supplied function.
   *
   * @param f stream handler
   *
   * @return value from applied handler
   */
  def withInputStream[T](f: InputStream => T): T =
    f(getInputStream)
}

/** Provides factory for `Entity`. */
object Entity {
  /** Creates `Entity` from supplied bytes. */
  def apply(bytes: Array[Byte]): Entity =
    ByteArrayEntity(notNull(bytes))

  /** Creates `Entity` from supplied input stream. */
  def apply(in: InputStream): Entity =
    InputStreamEntity(notNull(in))

  /**
   * Creates `Entity` from supplied writer.
   *
   * An output stream is passed to `writer`, and bytes written to the output
   * stream are used to build the entity.
   */
  def apply(writer: OutputStream => Unit): Entity =
    InputStreamEntity(new WriterInputStream(notNull(writer))(Auxiliary.executor))

  /** Creates `Entity` from supplied file. */
  def apply(file: File): Entity =
    FileEntity(notNull(file))

  /** Creates `Entity` from supplied text. */
  def apply(text: String, charset: String = "UTF-8"): Entity =
    ByteArrayEntity(text.getBytes(charset))

  /**
   * Creates `Entity` from supplied form data.
   *
   * @note The data is encoded as `application/x-www-form-urlencoded`.
   */
  def apply(data: Map[String, Seq[String]]): Entity =
    apply(QueryString.format(data))

  /**
   * Creates `Entity` from supplied form data.
   *
   * @note The data is encoded as `application/x-www-form-urlencoded`.
   */
  def apply(data: Seq[(String, String)]): Entity =
    apply(QueryString.format(data))

  /**
   * Creates `Entity` from supplied form data.
   *
   * @note The data is encoded as `application/x-www-form-urlencoded`.
   */
  def apply(one: (String, String), more: (String, String)*): Entity =
    apply(QueryString.format(one +: more))

  /**
   * Creates `Entity` from supplied query string.
   *
   * @note The query string is encoded as `application/x-www-form-urlencoded`.
   */
  def apply(query: QueryString): Entity =
    apply(query.toString)

  /** Creates `Entity` from supplied multipart form data. */
  def apply(multipart: Multipart, boundary: String): Entity =
    MultipartEntity(notNull(multipart), notNull(boundary))

  /** Gets empty `Entity`. */
  def empty: Entity = EmptyEntity
}

private object EmptyEntity extends Entity {
  val getLength = Some(0L)
  val getInputStream = EmptyInputStream
}

private case class ByteArrayEntity(bytes: Array[Byte]) extends Entity {
  val getLength = Some(bytes.length.toLong)
  val getInputStream = new ByteArrayInputStream(bytes)
}

private case class FileEntity(file: File) extends Entity {
  lazy val getLength = Some(file.length)
  lazy val getInputStream = new FileInputStream(file)
}

private case class InputStreamEntity(getInputStream: InputStream) extends Entity {
  val getLength = None
}

private case class MultipartEntity(multipart: Multipart, boundary: String) extends Entity {
  val getLength = None
  lazy val getInputStream = new WriterInputStream(writeMultipart)(Auxiliary.executor)

  private def writeMultipart(out: OutputStream): Unit = {
    val start = "--" + boundary
    val end = "--" + boundary + "--"

    multipart.parts.foreach { part =>
      out.writeLine(start)
      out.writeLine("Content-Disposition: " + part.contentDisposition.toString)

      if (!part.contentType.isText || part.contentType.subtype != "plain" || part.contentType.params.nonEmpty)
        out.writeLine("Content-Type: " + part.contentType.toString)
      out.writeLine()

      part match {
        case text: TextPart => out.writeLine(text.content)
        case file: FilePart =>
          file.content.withInputStream { in =>
            val buf = new Array[Byte](8192)
            var len = 0
            while ({ len = in.read(buf); len != -1 })
              out.write(buf, 0, len)
            out.writeLine()
          }
      }
    }

    out.writeLine(end)
    out.flush()
  }
}

