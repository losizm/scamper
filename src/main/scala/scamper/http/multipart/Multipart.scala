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

import java.io.File

/**
 * Represents multipart form data.
 *
 * @see [[Part]]
 */
sealed trait Multipart:
  /** Gets parts. */
  def parts: Seq[Part]

  /**
   * Collects parts into query string.
   *
   * @note The query string only includes parts whose types are `text/plain` and
   * whose disposition file name parameters are not set.
   */
  def toQuery: QueryString

  /**
   * Collects all parts into entity.
   *
   * @param boundary part delimiter
   */
  def toEntity(boundary: String): Entity

  /**
   * Gets first part with given name.
   *
   * @param name part name
   */
  def getPart(name: String): Option[Part]

  /**
   * Gets all parts with given name
   *
   * @param name part name
   */
  def getParts(name: String): Seq[Part]

  /**
   * Gets string content of first part with given name.
   *
   * @param name part name
   */
  def getString(name: String): Option[String] =
    getPart(name).map(_.getString())

  /**
   * Gets byte content for first part with given name.
   *
   * @param name part name
   */
  def getBytes(name: String): Option[Array[Byte]] =
    getPart(name).map(_.getBytes())

  /**
   * Gets file content of first part with given name.
   *
   * @param name part name
   */
  def getFile(name: String): Option[File] =
    getPart(name).map(_.getFile())

/** Provides factory for `Multipart`. */
object Multipart:
  private val random = java.security.SecureRandom()
  private val prefix = "----MultipartBoundary_"
  private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz"

  /** Creates multipart with supplied parts. */
  def apply(parts: Seq[Part]): Multipart =
    MultipartImpl(noNulls(parts, "parts"))

  /** Creates multipart with supplied parts. */
  def apply(one: Part, more: Part*): Multipart =
    apply(one +: more)

  /**
   * Gets body parser for multipart form data.
   *
   * @param dest destination directory in which file content is stored
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def bodyParser(dest: File = File(sys.props("java.io.tmpdir")), maxLength: Long = 8388608, bufferSize: Int = 8192): BodyParser[Multipart] =
    MultipartBodyParser(dest, maxLength.max(0), bufferSize.max(8192))

  /** Generates boundary. */
  def boundary(): String =
    prefix + String(random.ints(16, 0, 62).toArray.map(charset))

private case class MultipartImpl(parts: Seq[Part]) extends Multipart:
  private lazy val plainParts = parts.filter { part =>
    part.contentType.fullName == "text/plain" && part.fileName.isEmpty
  }

  lazy val toQuery: QueryString = QueryString(plainParts.map(part => part.name -> part.getString()))

  def toEntity(boundary: String): Entity =
    MultipartEntity(this, notNull(boundary, "boundary"))

  def getPart(name: String): Option[Part] =
    parts.collectFirst { case part if part.name == name => part }

  def getParts(name: String): Seq[Part] =
    parts.collect { case part if part.name == name => part }
