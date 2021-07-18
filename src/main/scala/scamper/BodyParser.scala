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

import java.io.{ File, InputStream }

import scala.collection.mutable.{ ArrayBuffer, ListBuffer }
import scala.util.Try

import scamper.headers.ContentType
import scamper.types.{ DispositionType, MediaType }

import Auxiliary.{ FileType, InputStreamType }

/** Provides utility for parsing message body. */
@FunctionalInterface
trait BodyParser[T]:
  /**
   * Parses body of supplied message.
   *
   * @return instance of `T`
   *
   * @throws java.io.IOException if error occurs while parsing
   */
  def parse(message: HttpMessage): T

/** Provides factory for `BodyParser`. */
object BodyParser:
  /**
   * Gets body parser for byte array.
   *
   * @param maxLength maximum length
   * @param bufferSize buffer size in bytes
   */
  def bytes(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[Array[Byte]] =
    ByteArrayBodyParser(maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for text.
   *
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def text(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[String] =
    TextBodyParser(maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for form data.
   *
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def form(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[Map[String, Seq[String]]] =
    FormBodyParser(maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for query string.
   *
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def query(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[QueryString] =
    QueryBodyParser(maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for multipart form data.
   *
   * @param dest destination directory in which file content is stored
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def multipart(dest: File = File(sys.props("java.io.tmpdir")), maxLength: Long = 8388608, bufferSize: Int = 8192): BodyParser[Multipart] =
    MultipartBodyParser(dest, maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for storing message body to file.
   *
   * @param dest destination to which message body is stored
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   *
   * @note If `dest` is a directory, then the parser creates a new file in the
   * specified directory on each parsing invocation. Otherwise, the parser
   * overwrites the specified file on each invocation.
   */
  def file(dest: File = File(sys.props("java.io.tmpdir")), maxLength: Long = 8388608, bufferSize: Int = 8192): BodyParser[File] =
    FileBodyParser(dest, maxLength.max(0), bufferSize.max(8192))

private class ByteArrayBodyParser(val maxLength: Long, bufferSize: Int) extends BodyParser[Array[Byte]] with BodyDecoder:
  def parse(message: HttpMessage): Array[Byte] =
    withDecode(message) { in =>
      val out = new ArrayBuffer[Byte](bufferSize)
      val buf = new Array[Byte](bufferSize)
      var len = 0
      var tot = 0

      while { len = in.read(buf); len != -1 } do
        tot += len
        if tot > maxLength then throw EntityTooLarge(maxLength)
        out ++= buf.take(len)

      out.toArray
    }

private class TextBodyParser(maxLength: Int, bufferSize: Int) extends BodyParser[String]:
  private val parser = ByteArrayBodyParser(maxLength, bufferSize)

  def parse(message: HttpMessage): String =
    message.getHeaderValue("Content-Type")
      .map(MediaType.apply)
      .flatMap(_.params.get("charset"))
      .orElse(Some("UTF-8"))
      .map(charset => String(parser.parse(message), charset))
      .get

private class QueryBodyParser(maxLength: Int, bufferSize: Int) extends BodyParser[QueryString]:
  private val parser = TextBodyParser(maxLength, bufferSize)

  def parse(message: HttpMessage): QueryString =
    QueryString(parser.parse(message))

private class FormBodyParser(maxLength: Int, bufferSize: Int) extends BodyParser[Map[String, Seq[String]]]:
  private val parser = QueryBodyParser(maxLength, bufferSize)

  def parse(message: HttpMessage): Map[String, Seq[String]] =
    parser.parse(message).toMap

private class FileBodyParser(dest: File, val maxLength: Long, bufferSize: Int) extends BodyParser[File] with BodyDecoder:
  def parse(message: HttpMessage): File =
    withDecode(message) { in =>
      val destFile = getDestFile()

      destFile.withOutputStream { out =>
        val buffer = new Array[Byte](bufferSize)
        var length = 0
        var total = 0

        while { length = in.read(buffer); length != -1 } do
          total += length
          if total > maxLength then throw EntityTooLarge(maxLength)
          out.write(buffer, 0, length)

        destFile
      }
    }

  private def getDestFile(): File =
    dest.isDirectory match
      case true  => File.createTempFile("scamper-dest-file-", ".tmp", dest)
      case false => dest

private class MultipartBodyParser(dest: File, val maxLength: Long, bufferSize: Int) extends BodyParser[Multipart] with BodyDecoder:
  private class Status(val boundary: String):
    val start = ("--" + boundary).getBytes("UTF-8")
    val end = ("--" + boundary + "--").getBytes("UTF-8")
    var continue = true

  def parse(message: HttpMessage): Multipart =
    val mediaType = message.contentType

    (mediaType.isMultipart && mediaType.subtype == "form-data") match
      case true =>
        mediaType.params.get("boundary")
          .map(boundary => getMultipart(decode(message), boundary))
          .getOrElse(throw HttpException("Missing boundary in Content-Type header"))

      case false =>
        throw HttpException(s"Expected multipart/form-data for Content-Type: $mediaType")

  private def getMultipart(in: InputStream, boundary: String): Multipart =
    val buffer = new Array[Byte](bufferSize)
    val status = Status(boundary)

    in.getLine(buffer) match
      case line if line.startsWith(String(status.start)) =>
        val parts = new ListBuffer[Part]

        while status.continue do
          val headers = HeaderStream.getHeaders(in, buffer)

          val disposition = headers.collectFirst {
            case header if header.name.equalsIgnoreCase("Content-Disposition") => DispositionType.parse(header.value)
          }.getOrElse(throw HeaderNotFound("Content-Disposition"))

          val contentType = headers.collectFirst {
            case header if header.name.equalsIgnoreCase("Content-Type") => MediaType(header.value)
          }.getOrElse(Auxiliary.textPlain)

          if contentType.isText && disposition.params.get("filename").isEmpty then
            val charset = contentType.params.getOrElse("charset", "UTF-8")
            parts += TextPart(headers, getTextContent(in, buffer, charset, status))
          else
            parts += FilePart(headers, getFileContent(in, buffer, status))

        Multipart(parts.toSeq)

      case line if line.startsWith(String(status.end)) => Multipart(Nil)

      case line => throw HttpException("Invalid start of multipart")

  private def getTextContent(in: InputStream, buffer: Array[Byte], charset: String, status: Status): String =
    var content = new ArrayBuffer[Byte]

    var length = in.readLine(buffer)
    if length == -1 then
      throw HttpException("Invalid part: truncation detected")

    while !buffer.startsWith(status.start) do
      content ++= buffer.take(length)

      length = in.readLine(buffer)
      if length == -1 then
        throw HttpException("Invalid part: truncation detected")

    if buffer.startsWith(status.end) then
      status.continue = false

    // Remove trailing crlf
    String(content.dropRight(2).toArray, charset)

  private def getFileContent(in: InputStream, buffer: Array[Byte], status: Status): File =
    val content = File.createTempFile("scamper-dest-file-", ".tmp", dest)

    content.withOutputStream { out =>
      var length = in.readLine(buffer)
      if length == -1 then
        throw HttpException("Invalid part: truncation detected")

      while !buffer.startsWith(status.start) do
        out.write(buffer, 0, length)

        length = in.readLine(buffer)
        if length == -1 then
          throw HttpException("Invalid part: truncation detected")

      if buffer.startsWith(status.end) then
        status.continue = false
    }

    // Remove trailing crlf
    val file = java.io.RandomAccessFile(content, "rw")
    try file.setLength(content.length - 2)
    finally Try(file.close())

    content

