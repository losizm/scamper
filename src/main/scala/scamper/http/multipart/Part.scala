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

/** Defines type alias for part content. */
type PartContent = String | Array[Byte] | File

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
   * Creates part using supplied content.
   *
   * @param name sets name
   * @param content sets content
   * @param contentType sets content type, which defaults based on content
   *   * if content is string, then defaults to `text/plain`
   *   * if content is byte array, then defaults to `application/octet-stream`
   *   * if content is file, then defaults based on content file name or
   *     `fileName` if provided
   * @param fileName sets file name, which defaults to `None`
   * @param params sets parameters, which defaults to `Map.empty`
   *
   * @return part
   */
  def apply(
    name: String,
    content: PartContent,
    contentType: Option[MediaType] = None,
    fileName: Option[String] = None,
    params: Map[String, String] = Map.empty,
  ): Part =
    notNull(name, "name")
    notNull(content, "content")
    notNull(contentType, "contentType")
    notNull(fileName, "fileName")
    notNull(params, "params")

    content match
      case content: String =>
        StringPart(
          getContentDisposition(name, fileName, params),
          contentType.getOrElse(MediaType.plain),
          content
        )

      case content: Array[Byte] =>
        ByteArrayPart(
          getContentDisposition(name, fileName, params),
          contentType.getOrElse(MediaType.octetStream),
          content
        )

      case content: File =>
        FilePart(
          getContentDisposition(name, fileName, params),
          contentType.getOrElse(getContentType(fileName.getOrElse(content.getName))),
          content
        )

  /**
   * Creates part using supplied content.
   *
   * @param contentDisposition sets content disposition
   * @param contentType sets content type
   * @param content sets content
   *
   * @return part
   */
  def apply(contentDisposition: DispositionType, contentType: MediaType, content: PartContent): Part =
    notNull(contentDisposition, "contentDisposition")
    notNull(contentType, "contentType")
    notNull(content, "content")

    if !contentDisposition.isFormData then
      throw HttpException("Content disposition is not form-data")

    if !contentDisposition.params.contains("name") then
      throw HttpException("Missing name parameter in content disposition")

    content match
      case content: String =>
        StringPart(contentDisposition, contentType, content)

      case content: Array[Byte] =>
        ByteArrayPart(contentDisposition, contentType, Arrays.copyOf(content, content.size))

      case content: File =>
        FilePart(contentDisposition, contentType, content)

  private def getContentDisposition(name: String, fileName: Option[String], params: Map[String, String]): DispositionType =
    fileName match
      case Some(fileName) => DispositionType("form-data", Map("name" -> name, "filename" -> fileName) ++ params)
      case None           => DispositionType("form-data", Map("name" -> name) ++ params)

  private def getFileName(content: File, fileName: String | Boolean): Option[String] =
    fileName match
      case fileName: String => Option(fileName)
      case true             => Option(content.getName)
      case false            => None

  private def getContentType(fileName: String): MediaType =
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
