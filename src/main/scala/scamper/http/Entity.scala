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

import java.io.*

import Values.notNull

/** Provides input stream to HTTP entity. */
trait Entity:
  /** Gets size in bytes if known. */
  def knownSize: Option[Long]

  /** Tests for known emptiness. */
  def isKnownEmpty: Boolean =
    knownSize.contains(0)

  /** Gets input stream to data. */
  def data: InputStream

  /**
   * Invokes function with input stream to data.
   *
   * @param f function
   *
   * @return applied function value
   */
  def withData[T](f: InputStream => T): T =
    f(data)

/** Provides factory for `Entity`. */
object Entity:
  /** Creates entity from supplied bytes. */
  def apply(bytes: Array[Byte]): Entity =
    ByteArrayEntity(notNull(bytes, "bytes"))

  /** Creates entity from supplied input stream. */
  def apply(in: InputStream): Entity =
    InputStreamEntity(notNull(in, "in"))

  /**
   * Creates entity from supplied writer.
   *
   * @note An output stream is passed to `writer`, and bytes written to the
   * output stream are used to build entity.
   */
  def apply(writer: OutputStream => Unit): Entity =
    InputStreamEntity(WriterInputStream(notNull(writer, "writer"))(using Auxiliary.executor))

  /** Creates entity from supplied file. */
  def apply(file: File): Entity =
    FileEntity(notNull(file, "file"))

  /** Creates entity from supplied text. */
  def apply(text: String, charset: String = "UTF-8"): Entity =
    ByteArrayEntity(text.getBytes(charset))

  /**
   * Creates entity from supplied form data.
   *
   * @note The data is encoded as `application/x-www-form-urlencoded`.
   */
  def apply(data: Map[String, Seq[String]]): Entity =
    apply(QueryString.format(data))

  /**
   * Creates entity from supplied form data.
   *
   * @note The data is encoded as `application/x-www-form-urlencoded`.
   */
  def apply(data: Seq[(String, String)]): Entity =
    apply(QueryString.format(data))

  /**
   * Creates entity from supplied form data.
   *
   * @note The data is encoded as `application/x-www-form-urlencoded`.
   */
  def apply(one: (String, String), more: (String, String)*): Entity =
    apply(QueryString.format(one +: more))

  /**
   * Creates entity from supplied query string.
   *
   * @note The query string is encoded as `application/x-www-form-urlencoded`.
   */
  def apply(query: QueryString): Entity =
    apply(query.toString)

  /** Gets empty entity. */
  def empty: Entity = EmptyEntity

private object EmptyEntity extends Entity:
  val knownSize = Some(0L)
  val data = EmptyInputStream

private case class ByteArrayEntity(bytes: Array[Byte]) extends Entity:
  val knownSize = Some(bytes.length.toLong)
  val data = ByteArrayInputStream(bytes)

private case class FileEntity(file: File) extends Entity:
  lazy val (data, knownSize) = (FileInputStream(file), Some(file.length))

private case class InputStreamEntity(data: InputStream) extends Entity:
  val knownSize = None
