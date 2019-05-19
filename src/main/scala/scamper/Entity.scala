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

import java.io.{ ByteArrayInputStream, File, FileInputStream, InputStream, OutputStream }
import java.nio.file.Path

import Auxiliary.{ FileType, OutputStreamType }

/** Representation of message body. */
trait Entity {
  /** Gets length in bytes if known. */
  def getLength(): Option[Long]

  /** Tests whether entity is known empty. */
  def isKnownEmpty(): Boolean =
    getLength().contains(0)

  /** Gets input stream to entity. */
  def getInputStream(): InputStream

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
  def fromBytes(bytes: Array[Byte]): Entity =
    ByteArrayEntity(bytes)

  /** Creates `Entity` with bytes from supplied input stream. */
  def fromInputStream(in: InputStream): Entity =
    InputStreamEntity(in)

  /** Creates `Entity` from bytes written to output stream. */
  def fromOutputStream(writer: OutputStream => Unit): Entity =
    InputStreamEntity(new WriterInputStream(writer)(Auxiliary.executor))

  /** Creates `Entity` with data from supplied file. */
  def fromFile(file: File): Entity =
    FileEntity(file)

  /** Creates `Entity` from string using specified character encoding. */
  def fromString(s: String, charset: String = "UTF-8"): Entity =
    ByteArrayEntity(s.getBytes(charset))

  /**
   * Creates `Entity` from supplied parameters.
   *
   * The parameters are encoded as `application/x-www-form-urlencoded`.
   */
  def fromParams(params: Map[String, Seq[String]]): Entity =
    fromString(QueryString.format(params))

  /**
   * Creates `Entity` from supplied parameters.
   *
   * The parameters are encoded as `application/x-www-form-urlencoded`.
   */
  def fromParams(params: (String, String)*): Entity =
    fromString(QueryString.format(params : _*))

  /**
   * Creates `Entity` from query.
   *
   * The query is encoded as `application/x-www-form-urlencoded`.
   */
  def fromQuery(query: QueryString): Entity =
    fromString(query.toString)

  /** Creates `Entity` from multipart form data using supplied boundary. */
  def fromMultipart(multipart: Multipart, boundary: String): Entity =
    MultipartEntity(multipart, boundary)

  /** Returns empty `Entity`. */
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

