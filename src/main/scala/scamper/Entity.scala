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
  /** The length in bytes, if known. */
  def length: Option[Long]

  /** Tests whether entity is known to be empty. */
  def isKnownEmpty: Boolean =
    length.contains(0)

  /** Gets input stream to entity. */
  def getInputStream: InputStream

  /**
   * Provides access to input stream with automatic resource management.
   *
   * The input stream is passed to supplied function, and stream is closed upon
   * function's return.
   *
   * @return value returned from supplied function
   */
  def withInputStream[T](f: InputStream => T): T = {
    val in = getInputStream
    try f(in)
    finally Try(in.close())
  }
}

/** Entity factory */
object Entity {
  /** Creates entity whose content is supplied bytes. */
  def apply(bytes: Array[Byte]): Entity =
    apply(bytes, 0, bytes.length)

  /** Creates entity whose content is supplied bytes. */
  def apply(bytes: Array[Byte], start: Int, length: Int): Entity = {
    require(start >= 0, "Start must be nonnegative")
    require(start + length <= bytes.length, "Applied start and length must not exceed data length")

    val copy = new Array[Byte](length)
    bytes.copyToArray(copy, start, length)
    ByteArrayEntity(copy)
  }

  /** Creates entity with input stream returned from supplied function. */
  def apply(f: () => InputStream): Entity =
    InputStreamEntity(f)

  /** Creates entity whose content is data in supplied file. */
  def apply(file: File): Entity =
    FileEntity(file)

  /** Creates entity whose content is data in file at supplied path. */
  def apply(path: Path): Entity =
    FileEntity(path.toFile)

  /** Creates entity whose content is UTF-8 encoded bytes of supplied text. */
  def apply(text: String): Entity =
    ByteArrayEntity(text.getBytes("UTF-8"))

  /** Creates entity whose content is encoded bytes of supplied text. */
  def apply(text: String, charset: String): Entity =
    ByteArrayEntity(text.getBytes(charset))

  /** Creates entity whose content is encoded bytes of supplied text. */
  def apply(text: String, charset: Charset): Entity =
    ByteArrayEntity(text.getBytes(charset))

  /** Creates entity whose content is supplied form data. */
  def apply(form: Map[String, Seq[String]]): Entity =
    Entity(QueryParams.format(form))

  /** Creates entity whose content is supplied form data. */
  def apply(form: (String, String)*): Entity =
    Entity(QueryParams.format(form : _*))

  /** Creates empty entity. */
  def empty: Entity = ByteArrayEntity(Array.empty)
}

private case class InputStreamEntity(f: () => InputStream) extends Entity {
  val length = None
  def getInputStream = f()
}

private case class ByteArrayEntity(bytes: Array[Byte]) extends Entity {
  val length = Some(bytes.length)
  def getInputStream = new ByteArrayInputStream(bytes)
}

private case class FileEntity(file: File) extends Entity {
  def length = Some(file.length)
  def getInputStream = new FileInputStream(file)
}

