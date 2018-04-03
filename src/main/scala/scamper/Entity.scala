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
import java.nio.charset.Charset
import scala.util.Try

/** HTTP entity */
trait Entity {
  /** Length in bytes if known */
  def length: Option[Long]

  /** Tests whether entity is known to be empty. */
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
   * @return result of supplied function
   */
  def withInputStream[T](f: InputStream => T): T = {
    val in = getInputStream
    try f(in)
    finally Try(in.close())
  }
}

/** Entity factory */
object Entity {
  /** Creates entity with supplied bytes. */
  def apply(bytes: Array[Byte]): Entity =
    apply(bytes, 0, bytes.length)

  /** Creates entity with supplied bytes. */
  def apply(bytes: Array[Byte], start: Int, length: Int): Entity = {
    require(start >= 0, "Start must be nonnegative")
    require(start + length <= bytes.length, "Applied start and length must not exceed data length")

    val copy = new Array[Byte](length)
    bytes.copyToArray(copy, start, length)
    ByteArrayEntity(copy)
  }

  /** Creates entity with input stream from supplied function. */
  def apply(f: () => InputStream): Entity =
    InputStreamEntity(f)

  /** Creates entity with data from supplied file. */
  def apply(file: File): Entity =
    FileEntity(file)

  /** Creates entity with data from file at supplied path. */
  def apply(path: Path): Entity =
    FileEntity(path.toFile)

  /** Creates entity with UTF-8 encoded bytes of supplied text. */
  def apply(text: String): Entity =
    ByteArrayEntity(text.getBytes("UTF-8"))

  /** Creates entity with encoded bytes of supplied text. */
  def apply(text: String, charset: String): Entity =
    ByteArrayEntity(text.getBytes(charset))

  /** Creates entity with encoded bytes of supplied text. */
  def apply(text: String, charset: Charset): Entity =
    ByteArrayEntity(text.getBytes(charset))

  /** Creates entity with URL encoded value of supplied form data. */
  def apply(form: Map[String, Seq[String]]): Entity =
    Entity(QueryParams.format(form))

  /** Creates entity with URL encoded value of supplied form data. */
  def apply(form: (String, String)*): Entity =
    Entity(QueryParams.format(form : _*))

  /** Creates empty entity. */
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

private case class InputStreamEntity(f: () => InputStream) extends Entity {
  val length = None
  lazy val getInputStream = f()
}
