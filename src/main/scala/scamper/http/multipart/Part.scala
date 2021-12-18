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

import java.io.{ ByteArrayInputStream, File, FileInputStream, InputStream }
import java.util.Arrays

import scamper.http.types.{ DispositionType, MediaType }

import Validate.notNull

/**
 * Represents part in multipart form data.
 *
 * @see [[Multipart]]
 */
sealed trait Part:
  /** Gets content disposition. */
  def contentDisposition: DispositionType

  /** Gets content type. */
  def contentType: MediaType

  /** Gets name. */
  def name: String

  /** Gets file name. */
  def fileName: Option[String]

  /** Gets size. */
  def size: Long

  /** Gets string content. */
  def getString(): String

  /** Gets byte content. */
  def getBytes(): Array[Byte]

  /** Gets file content. */
  def getFile(): File

  /**
   * Passes content input stream to supplied function.
   *
   * @return result of supplied function
   */
  def withInputStream[T](f: InputStream => T): T

/** Provides part factory. */
object Part:
  /**
   * Creates part using supplied string content. */
  def apply(name: String, content: String): Part =
    notNull(name, "name")
    notNull(content, "content")

    StringPart(getDispositionType(name, None), MediaType.plain, content)

  /**
   * Creates part using supplied byte content.
   *
   * @note Content is copied.
   */
  def apply(name: String, content: Array[Byte]): Part =
    notNull(name, "name")
    notNull(content, "content")

    ByteArrayPart(getDispositionType(name, None), MediaType.octetStream, Arrays.copyOf(content, content.length))

  /**
   * Creates part using supplied byte content.
   *
   * @note Content is copied.
   */
  def apply(name: String, content: Array[Byte], offset: Int, length: Int): Part =
    notNull(name, "name")
    notNull(content, "content")

    ByteArrayPart(getDispositionType(name, None), MediaType.octetStream, Arrays.copyOfRange(content, offset, offset + length))

  /** Creates part using supplied file content. */
  def apply(name: String, content: File): Part =
    notNull(name, "name")
    notNull(content, "content")

    FilePart(getDispositionType(name, Some(content.getName)), getMediaType(content.getName), content)

  /** Creates part using supplied file content and optional file name. */
  def apply(name: String, content: File, fileName: Option[String]): Part =
    notNull(name, "name")
    notNull(content, "content")
    notNull(fileName, "fileName")

    FilePart(getDispositionType(name, fileName), getMediaType(fileName.getOrElse(content.getName)), content)

  /** Creates part using supplied string content. */
  def apply(contentDisposition: DispositionType, contentType: MediaType, content: String): Part =
    notNull(contentDisposition, "contentDisposition")
    notNull(contentType, "contentType")
    notNull(content, "content")

    if !contentDisposition.isFormData then
      throw HttpException("Content disposition is not form-data")

    if !contentDisposition.params.contains("name") then
      throw HttpException("Missing name parameter in content disposition")

    StringPart(contentDisposition, contentType, content)

  /** Creates part using supplied byte content. */
  def apply(contentDisposition: DispositionType, contentType: MediaType, content: Array[Byte]): Part =
    notNull(contentDisposition, "contentDisposition")
    notNull(contentType, "contentType")
    notNull(content, "content")

    if !contentDisposition.isFormData then
      throw HttpException("Content disposition is not form-data")

    if !contentDisposition.params.contains("name") then
      throw HttpException("Missing name parameter in content disposition")

    ByteArrayPart(contentDisposition, contentType, content)

  /** Creates part using supplied file content. */
  def apply(contentDisposition: DispositionType, contentType: MediaType, content: File): Part =
    notNull(contentDisposition, "contentDisposition")
    notNull(contentType, "contentType")
    notNull(content, "content")

    if !contentDisposition.isFormData then
      throw HttpException("Content disposition is not form-data")

    if !contentDisposition.params.contains("name") then
      throw HttpException("Missing name parameter in content disposition")

    FilePart(contentDisposition, contentType, content)

  private def getDispositionType(name: String, fileName: Option[String]): DispositionType =
    fileName match
      case Some(value) => DispositionType("form-data", "name" -> name, "filename" -> value)
      case None        => DispositionType("form-data", "name" -> name)

  private def getMediaType(fileName: String): MediaType =
    MediaType.forFileName(fileName).getOrElse(MediaType.octetStream)

private sealed abstract class AbstractPart extends Part:
  protected lazy val charset = contentType.params.getOrElse("charset", "UTF-8")

  lazy val name     = contentDisposition.params("name")
  lazy val fileName = contentDisposition.params.get("filename")

private case class StringPart(contentDisposition: DispositionType, contentType: MediaType, content: String) extends AbstractPart:
  private lazy val bytes = content.getBytes(charset)
  private lazy val file  = File.createTempFile("scamper-part-file", ".tmp").setBytes(bytes)

  lazy val size = bytes.size

  def getString() = content
  def getBytes()  = bytes
  def getFile()   = file

  def withInputStream[T](f: InputStream => T) =
    val in = ByteArrayInputStream(bytes)
    try f(in) finally in.close()

private case class ByteArrayPart(contentDisposition: DispositionType, contentType: MediaType, content: Array[Byte]) extends AbstractPart:
  private lazy val string = String(content, charset)
  private lazy val file   = File.createTempFile("scamper-part-file", ".tmp").setBytes(content)

  lazy val size = content.size

  def getString() = string
  def getBytes()  = content
  def getFile()   = file

  def withInputStream[T](f: InputStream => T) =
    val in = ByteArrayInputStream(content)
    try f(in) finally in.close()

private case class FilePart(contentDisposition: DispositionType, contentType: MediaType, content: File) extends AbstractPart:
  private lazy val bytes  = content.getBytes()
  private lazy val string = String(bytes, charset)

  lazy val size = content.length

  def getString() = string
  def getBytes()  = bytes
  def getFile()   = content

  def withInputStream[T](f: InputStream => T) =
    content.withInputStream(f)
