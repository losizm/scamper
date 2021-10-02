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
package multipart

import java.io.File

import scamper.types.{ DispositionType, MediaType }

import Validate.{ noNulls, notNull }

/**
 * Represents multipart form data.
 *
 * @see [[Part]]
 */
sealed trait Multipart:
  /** Gets parts. */
  def parts: Seq[Part]

  /** Gets all text parts. */
  def textParts: Seq[TextPart]

  /** Gets all file parts. */
  def fileParts: Seq[FilePart]

  /** Collects all text parts into query string. */
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
   * Gets first part with given name and casts it to `TextPart`.
   *
   * @param name part name
   *
   * @throws java.lang.ClassCastException if part is present and is not text
   */
  def getTextPart(name: String): Option[TextPart]

  /**
   * Gets text content of first part with given name.
   *
   * @param name part name
   *
   * @throws java.lang.ClassCastException if part is present and is not text
   */
  def getText(name: String): Option[String]

  /**
   * Gets first part with given name and casts it to `FilePart`.
   *
   * @param name part name
   *
   * @throws java.lang.ClassCastException if part is present and is not file
   */
  def getFilePart(name: String): Option[FilePart]

  /**
   * Gets file content of first part with given name.
   *
   * @param name part name
   *
   * @throws java.lang.ClassCastException if part is present and is not file
   */
  def getFile(name: String): Option[File]

/**
 * Represents part in multipart form data.
 *
 * @see [[Multipart]]
 */
sealed trait Part:
  /** Gets name. */
  def name: String

  /** Gets Content-Disposition header value. */
  def contentDisposition: DispositionType

  /** Gets Content-Type header value. */
  def contentType: MediaType

/**
 * Represents text content in multipart form data.
 *
 * @see [[FilePart]]
 */
trait TextPart extends Part:
  /** Gets text content. */
  def content: String

/**
 * Represents file content in multipart form data.
 *
 * @see [[TextPart]]
 */
trait FilePart extends Part:
  /** Gets file content. */
  def content: File

  /** Gets file name specified in Content-Disposition header. */
  def getFileName: Option[String]

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
  def getBodyParser(dest: File = File(sys.props("java.io.tmpdir")), maxLength: Long = 8388608, bufferSize: Int = 8192): BodyParser[Multipart] =
    MultipartBodyParser(dest, maxLength.max(0), bufferSize.max(8192))

  /** Generates boundary. */
  def boundary(): String =
    prefix + String(random.ints(16, 0, 62).toArray.map(charset))

/** Provides factory for `TextPart`. */
object TextPart:
  /** Creates text part with given name and content. */
  def apply(name: String, content: String): TextPart =
    apply(DispositionType("form-data", "name" -> name), content)

  /** Creates text part with supplied disposition and content. */
  def apply(contentDisposition: DispositionType, content: String): TextPart =
    apply(contentDisposition, Auxiliary.textPlain, content)

  /** Creates text part with supplied disposition, content type, and content. */
  def apply(contentDisposition: DispositionType, contentType: MediaType, content: String): TextPart =
    if !contentDisposition.isFormData then
      throw HttpException("Content disposition is not form-data")

    if !contentType.isText then
      throw HttpException("Content type is not text")

    val name = contentDisposition.params.get("name")
      .getOrElse(throw HttpException("Missing name parameter in content disposition"))

    TextPartImpl(name, content, contentDisposition, contentType)

  /** Creates text part with supplied headers and content. */
  def apply(headers: Seq[Header], content: String): TextPart =
    val contentDisposition = headers.collectFirst {
      case header if header.name.equalsIgnoreCase("Content-Disposition") => DispositionType.parse(header.value)
    }.getOrElse(throw HeaderNotFound("Content-Disposition"))

    val contentType = headers.collectFirst {
      case header if header.name.equalsIgnoreCase("Content-Type") => MediaType(header.value)
    }.getOrElse(Auxiliary.textPlain)

    apply(contentDisposition, contentType, notNull(content, "content"))

/** Provides factory for `FilePart`. */
object FilePart:
  /** Creates file part with given name and content. */
  def apply(name: String, content: File): FilePart =
    apply(getDisposition(name, Some(content.getName)), getType(content), content)

  /** Creates file part with given name, content, and file name. */
  def apply(name: String, content: File, fileName: String): FilePart =
    apply(getDisposition(name, Some(fileName)), getType(content), content)

  /** Creates file part with given name, content, and optional file name. */
  def apply(name: String, content: File, fileName: Option[String]): FilePart =
    apply(getDisposition(name, fileName), getType(content), content)

  /** Creates file part from supplied disposition and content. */
  def apply(contentDisposition: DispositionType, content: File): FilePart =
    apply(contentDisposition, getType(content), content)

  /** Creates file part from supplied disposition, content type, and content. */
  def apply(contentDisposition: DispositionType, contentType: MediaType, content: File): FilePart =
    if !contentDisposition.isFormData then
      throw HttpException("Content disposition is not form-data")

    val name = contentDisposition.params.get("name")
      .getOrElse(throw HttpException("Missing name parameter in content disposition"))

    FilePartImpl(name, notNull(content, "content"), contentDisposition, contentType)

  /** Creates file part from supplied headers and content. */
  def apply(headers: Seq[Header], content: File): FilePart =
    val contentDisposition = headers.collectFirst {
      case header if header.name.equalsIgnoreCase("Content-Disposition") => DispositionType.parse(header.value)
    }.getOrElse(throw HeaderNotFound("Content-Disposition"))

    val contentType = headers.collectFirst {
      case header if header.name.equalsIgnoreCase("Content-Type") => MediaType(header.value)
    }.getOrElse(getType(content))

    apply(contentDisposition, contentType, content)

  private def getDisposition(name: String, optFileName: Option[String]): DispositionType =
    optFileName.map(fileName => DispositionType("form-data", "name" -> name, "filename" -> fileName))
      .getOrElse(DispositionType("form-data", "name" -> name))

  private def getType(content: File): MediaType =
    MediaType.forFile(content).getOrElse(Auxiliary.applicationOctetStream)

private case class MultipartImpl(parts: Seq[Part]) extends Multipart:
  lazy val textParts: Seq[TextPart] =
    parts.collect { case part: TextPart => part }

  lazy val fileParts: Seq[FilePart] =
    parts.collect { case part: FilePart => part }

  lazy val toQuery: QueryString =
    QueryString(textParts.map(part => part.name -> part.content))

  def toEntity(boundary: String): Entity =
    MultipartEntity(this, notNull(boundary, "boundary"))

  def getPart(name: String): Option[Part] =
    parts.collectFirst { case part if part.name == name => part }

  def getParts(name: String): Seq[Part] =
    parts.collect { case part if part.name == name => part }

  def getTextPart(name: String): Option[TextPart] =
    getPart(name).map(_.asInstanceOf[TextPart])

  def getText(name: String): Option[String] =
    getTextPart(name).map(_.content)

  def getFilePart(name: String): Option[FilePart] =
    getPart(name).map(_.asInstanceOf[FilePart])

  def getFile(name: String): Option[File] =
    getFilePart(name).map(_.content)

private case class TextPartImpl(
  name:               String,
  content:            String,
  contentDisposition: DispositionType,
  contentType:        MediaType
) extends TextPart

private case class FilePartImpl(
  name:               String,
  content:            File,
  contentDisposition: DispositionType,
  contentType:        MediaType
) extends FilePart:
  val getFileName: Option[String] = contentDisposition.params.get("filename")
