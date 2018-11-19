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

import java.io.{ ByteArrayInputStream, File, FileInputStream, InputStream }
import java.nio.file.Path
import scala.util.Try

/** Representation of message body. */
trait Entity {
  /** Gets length in bytes if known. */
  def length: Option[Long]

  /** Tests whether entity is known empty. */
  def isKnownEmpty: Boolean =
    length.contains(0)

  /** Gets input stream to entity. */
  def getInputStream(): InputStream

  /**
   * Provides access to input stream with automatic resource management.
   *
   * Input stream is passed to supplied function and closed on function's
   * return.
   *
   * @param f stream handler
   *
   * @return value from applied handler
   */
  def withInputStream[T](f: InputStream => T): T = {
    val in = getInputStream
    try f(in)
    finally Try(in.close())
  }
}

/** Provided factory for `Entity`. */
object Entity {
  /** Creates `Entity` with supplied bytes. */
  def apply(bytes: Array[Byte]): Entity =
    ByteArrayEntity(bytes)

  /** Creates `Entity` with bytes from supplied input stream. */
  def apply(in: InputStream): Entity =
    InputStreamEntity(in)

  /** Creates `Entity` with data from supplied file. */
  def apply(file: File): Entity =
    FileEntity(file)

  /** Creates `Entity` with data at supplied path. */
  def apply(path: Path): Entity =
    FileEntity(path.toFile)

  /**
   * Creates `Entity` with supplied text.
   *
   * The text is encoded using `UTF-8` charset.
   */
  def apply(text: String): Entity =
    ByteArrayEntity(text.getBytes("UTF-8"))

  /**
   * Creates `Entity` with supplied form data.
   *
   * The form data is converted to `application/x-www-form-urlencoded` format.
   */
  def apply(form: Map[String, Seq[String]]): Entity =
    apply(QueryParams.format(form))

  /**
   * Creates `Entity` with supplied form data.
   *
   * The form data is converted to `application/x-www-form-urlencoded` format.
   */
  def apply(form: (String, String)*): Entity =
    apply(QueryParams.format(form : _*))

  /** Creates empty `Entity`. */
  def empty(): Entity = ByteArrayEntity(Array.empty)
}

private case class ByteArrayEntity(bytes: Array[Byte]) extends Entity {
  val length = Some(bytes.length)
  val getInputStream = new ByteArrayInputStream(bytes)
}

private case class FileEntity(file: File) extends Entity {
  lazy val length = Some(file.length)
  lazy val getInputStream = new FileInputStream(file)
}

private case class InputStreamEntity(getInputStream: InputStream) extends Entity {
  val length = None
}
