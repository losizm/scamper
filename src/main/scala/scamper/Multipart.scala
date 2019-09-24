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

import java.io.{ File, FileInputStream, InputStream }

import scamper.types.{ DispositionType, MediaType }

/**
 * Represents multipart form data.
 *
 * @see [[Part]]
 */
trait Multipart {
  /** Gets parts. */
  def parts: Seq[Part]

  /** Gets all text parts. */
  lazy val textParts: Seq[TextPart] =
    parts.collect { case part: TextPart => part }

  /** Gets all file parts. */
  lazy val fileParts: Seq[FilePart] =
    parts.collect { case part: FilePart => part }

  /** Collects all text parts into query string. */
  lazy val toQuery: QueryString =
    QueryString(textParts.map { part => part.name -> part.content } : _*)

  /**
   * Gets first part with given name.
   *
   * @param name part name
   */
  def getPart(name: String): Option[Part] =
    parts.collectFirst { case part if part.name == name => part }

  /**
   * Gets all parts with given name
   *
   * @param name part name
   */
  def getParts(name: String): Seq[Part] =
    parts.collect { case part if part.name == name => part }

  /**
   * Gets first part with given name and casts it to `TextPart`.
   *
   * @param name part name
   *
   * @throws ClassCastException if part is present and is not text part
   */
  def getTextPart(name: String): Option[TextPart] =
    getPart(name).map(_.asInstanceOf[TextPart])

  /**
   * Gets text content of first part with given name.
   *
   * @param name part name
   *
   * @throws ClassCastException if part is present and is not text part
   */
  def getText(name: String): Option[String] =
    getTextPart(name).map(_.content)

  /**
   * Gets first part with given name and casts it to `FilePart`.
   *
   * @param name part name
   *
   * @throws ClassCastException if part is present and is not file part
   */
  def getFilePart(name: String): Option[FilePart] =
    getPart(name).map(_.asInstanceOf[FilePart])

  /**
   * Gets file content of first part with given name.
   *
   * @param name part name
   *
   * @throws ClassCastException if part is present and is not file part
   */
  def getFile(name: String): Option[File] =
    getFilePart(name).map(_.content)
}

/**
 * Represents part in multipart form data.
 *
 * @see [[Multipart]]
 */
sealed trait Part {
  /** Gets name. */
  def name: String

  /** Gets Content-Disposition header value. */
  def contentDisposition: DispositionType

  /** Gets Content-Type header value. */
  def contentType: MediaType
}

/**
 * Represents text content in multipart form data.
 *
 * @see [[FilePart]]
 */
trait TextPart extends Part {
  /** Gets text content. */
  def content: String
}

/**
 * Represents file content in multipart form data.
 *
 * @see [[TextPart]]
 */
trait FilePart extends Part {
  /** Gets file content. */
  def content: File

  /** Gets file name specified in Content-Disposition header. */
  def getFileName: Option[String]
}

/** Provides factory for Multipart. */
object Multipart {
  private val random = new java.security.SecureRandom()
  private val prefix = "----ScamperMultipartBoundary_"
  private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz"

  /** Creates Multipart with supplied parts. */
  def apply(parts: Part*): Multipart = MultipartImpl(parts)

  /** Generates boundary. */
  def boundary(): String = prefix + new String(random.ints(16, 0, 62).toArray.map(charset))
}

/** Provides factory for TextPart. */
object TextPart {
  /** Creates TextPart with given name and content. */
  def apply(name: String, content: String): TextPart =
    apply(DispositionType("form-data", "name" -> name), content)

  /** Creates TextPart with supplied disposition and content. */
  def apply(contentDisposition: DispositionType, content: String): TextPart =
    apply(contentDisposition, Auxiliary.`text/plain`, content)

  /** Creates TextPart with supplied disposition, content type, and content. */
  def apply(contentDisposition: DispositionType, contentType: MediaType, content: String): TextPart = {
    if (!contentDisposition.isFormData)
      throw new HttpException("Content disposition is not form-data")

    if (!contentType.isText)
      throw new HttpException("Content type is not text")

    val name = contentDisposition.params.get("name")
      .getOrElse(throw new HttpException("Missing name parameter in content disposition"))

    TextPartImpl(name, content, contentDisposition, contentType)
  }

  /** Creates TextPart with supplied headers and content. */
  def apply(headers: Seq[Header], content: String): TextPart = {
    val contentDisposition = headers.collectFirst {
      case Header(name, value) if name.equalsIgnoreCase("Content-Disposition") => DispositionType.parse(value)
    }.getOrElse(throw HeaderNotFound("Content-Disposition"))

    val contentType = headers.collectFirst {
      case Header(name, value) if name.equalsIgnoreCase("Content-Type") => MediaType.parse(value)
    }.getOrElse(Auxiliary.`text/plain`)

    apply(contentDisposition, contentType, content)
  }
}

/** Provides factory for FilePart. */
object FilePart {
  /** Creates FilePart with given name and content. */
  def apply(name: String, content: File): FilePart =
    apply(getDisposition(name, Some(content.getName)), getType(content), content)

  /** Creates FilePart with given name, file name, and content. */
  def apply(name: String, content: File, fileName: String): FilePart =
    apply(getDisposition(name, Some(fileName)), getType(content), content)

  /** Creates FilePart with given name and content. */
  def apply(name: String, content: File, fileName: Option[String]): FilePart =
    apply(getDisposition(name, fileName), getType(content), content)

  /** Creates FilePart from supplied disposition and content. */
  def apply(contentDisposition: DispositionType, content: File): FilePart =
    apply(contentDisposition, getType(content), content)

  /** Creates FilePart from supplied disposition, content type, and content. */
  def apply(contentDisposition: DispositionType, contentType: MediaType, content: File): FilePart = {
    if (!contentDisposition.isFormData)
      throw new HttpException("Content disposition is not form-data")

    val name = contentDisposition.params.get("name")
      .getOrElse(throw new HttpException("Missing name parameter in content disposition"))

    FilePartImpl(name, content, contentDisposition, contentType)
  }

  /** Creates FilePart from supplied headers and content. */
  def apply(headers: Seq[Header], content: File): FilePart = {
    val contentDisposition = headers.collectFirst {
      case Header(name, value) if name.equalsIgnoreCase("Content-Disposition") => DispositionType.parse(value)
    }.getOrElse(throw HeaderNotFound("Content-Disposition"))

    val contentType = headers.collectFirst {
      case Header(name, value) if name.equalsIgnoreCase("Content-Type") => MediaType.parse(value)
    }.getOrElse(getType(content))

    apply(contentDisposition, contentType, content)
  }

  private def getDisposition(name: String, optFileName: Option[String]): DispositionType =
    optFileName.map(fileName => DispositionType("form-data", "name" -> name, "filename" -> fileName))
      .getOrElse(DispositionType("form-data", "name" -> name))

  private def getType(content: File): MediaType =
    MediaType.fromFile(content).getOrElse(Auxiliary.`application/octet-stream`)
}

private case class MultipartImpl(parts: Seq[Part]) extends Multipart

private case class TextPartImpl(name: String, content: String, contentDisposition: DispositionType, contentType: MediaType) extends TextPart

private case class FilePartImpl(name: String, content: File, contentDisposition: DispositionType, contentType: MediaType) extends FilePart {
  val getFileName: Option[String] = contentDisposition.params.get("filename")
}

