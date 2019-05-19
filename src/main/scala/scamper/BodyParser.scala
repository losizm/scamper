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

import java.io.{ File, InputStream }

import scala.collection.mutable.{ ArrayBuffer, ListBuffer }
import scala.util.Try

import scamper.headers.ContentType
import scamper.types.{ DispositionType, MediaType }

import Auxiliary.{ FileType, InputStreamType }

/**
 * Provides utility for parsing HTTP message body.
 *
 * @see [[BodyParsers]]
 */
trait BodyParser[T] {
  /** Parses body of supplied message and returns instance of defined type. */
  def apply(message: HttpMessage): T
}

/** Includes default body parser implementations. */
object BodyParsers {
  /**
   * Gets body parser for collecting raw bytes.
   *
   * @param maxLength maximum length
   */
  def bytes(maxLength: Int = 8388608): BodyParser[Array[Byte]] =
    new ByteArrayBodyParser(maxLength.max(0))

  /**
   * Gets body parser for collecting text.
   *
   * @param maxLength maximum length in bytes
   */
  def text(maxLength: Int = 8388608): BodyParser[String] =
    new TextBodyParser(maxLength.max(0))

  /**
   * Gets body parser for collecting form data.
   *
   * @param maxLength maximum length in bytes
   */
  def form(maxLength: Int = 8388608): BodyParser[Map[String, Seq[String]]] =
    new FormBodyParser(maxLength.max(0))

  /**
   * Gets body parser for collecting form data as query.
   *
   * @param maxLength maximum length in bytes
   */
  def query(maxLength: Int = 8388608): BodyParser[QueryString] =
    new QueryBodyParser(maxLength.max(0))

  /**
   * Gets body parser for collecting multipart form data.
   *
   * @param dest destination directory in which file content is stored
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def multipart(dest: File = new File(sys.props("java.io.tmpdir")), maxLength: Long = 8388608, bufferSize: Int = 8192): BodyParser[Multipart] =
    new MultipartBodyParser(dest, maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for storing message body to file.
   *
   * If `dest` is a directory, then the parser creates a new file in the
   * specified directory on each parsing invocation. Otherwise, the parser
   * overwrites the specified file on each invocation.
   *
   * @param dest destination to which message body is stored
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def file(dest: File = new File(sys.props("java.io.tmpdir")), maxLength: Long = 8388608, bufferSize: Int = 8192): BodyParser[File] =
    new FileBodyParser(dest, maxLength.max(0), bufferSize.max(8192))
}

private class ByteArrayBodyParser(val maxLength: Long) extends BodyParser[Array[Byte]] with BodyParsing {
  val bufferSize = maxLength.min(8192).toInt

  def apply(message: HttpMessage): Array[Byte] =
    withInputStream(message) { in =>
      val out = new ArrayBuffer[Byte](bufferSize)
      val buf = new Array[Byte](bufferSize)
      var len = 0
      var tot = 0

      while ({ len = in.read(buf); len != -1 }) {
        tot += len
        if (tot > maxLength) throw new HttpException(s"Entity too large: length > $maxLength")
        out ++= buf.take(len)
      }

      out.toArray
    }
}

private class TextBodyParser(maxLength: Int) extends BodyParser[String] {
  private val parser = new ByteArrayBodyParser(maxLength)

  def apply(message: HttpMessage): String =
    message.getHeaderValue("Content-Type")
      .map(MediaType.parse)
      .flatMap(_.params.get("charset"))
      .orElse(Some("UTF-8"))
      .map(charset => new String(parser(message), charset)).get
}

private class QueryBodyParser(maxLength: Int) extends BodyParser[QueryString] {
  private val parser = new TextBodyParser(maxLength)

  def apply(message: HttpMessage): QueryString =
    QueryString(parser(message))
}

private class FormBodyParser(maxLength: Int) extends BodyParser[Map[String, Seq[String]]] {
  private val parser = new TextBodyParser(maxLength)

  def apply(message: HttpMessage): Map[String, Seq[String]] =
    QueryString.parse(parser(message))
}

private class FileBodyParser(val dest: File, val maxLength: Long, val bufferSize: Int) extends BodyParser[File] with BodyParsing {
  def apply(message: HttpMessage): File =
    withInputStream(message) { in =>
      val destFile = getDestFile()

      destFile.withOutputStream { out =>
        val buffer = new Array[Byte](bufferSize)
        var length = 0

        while ({ length = in.read(buffer); length != -1 })
          out.write(buffer, 0, length)

        destFile
      }
    }

  private def getDestFile(): File =
    dest.isDirectory match {
      case true  => File.createTempFile("scamper-dest-file-", ".tmp", dest)
      case false => dest
    }
}

private class MultipartBodyParser(val dest: File, val maxLength: Long, val bufferSize: Int) extends BodyParser[Multipart] with BodyParsing {
  private class Status(val boundary: String) {
    val start = ("--" + boundary).getBytes("UTF-8")
    val end = ("--" + boundary + "--").getBytes("UTF-8")
    var continue = true
  }

  def apply(message: HttpMessage): Multipart =
    message.contentType match {
      case MediaType("multipart", "form-data", params) =>
        params.get("boundary")
          .map { boundary => withInputStream(message) { in => getMultipart(in, boundary) } }
          .getOrElse(throw new HttpException("Missing boundary in Content-Type header"))

      case value => throw new HttpException(s"Content-Type is not multipart/form-data: $value")
    }

  private def getMultipart(in: InputStream, boundary: String): Multipart = {
    val buffer = new Array[Byte](bufferSize)
    val status = new Status(boundary)

    in.getLine(buffer) match {
      case line if line.startsWith(new String(status.start)) =>
        val parts = new ListBuffer[Part]

        while (status.continue) {
          val headers = HeaderStream.getHeaders(in, buffer)

          val disposition = headers.collectFirst {
            case Header(name, value) if name.equalsIgnoreCase("Content-Disposition") => DispositionType.parse(value)
          }.getOrElse(throw HeaderNotFound("Content-Disposition"))

          val contentType = headers.collectFirst {
            case Header(name, value) if name.equalsIgnoreCase("Content-Type") => MediaType.parse(value)
          }.getOrElse(Auxiliary.`text/plain`)

          if (contentType.isText && disposition.params.get("filename").isEmpty) {
            val charset = contentType.params.getOrElse("charset", "UTF-8")
            parts += TextPart(headers, getTextContent(in, buffer, charset, status))
          } else {
            parts += FilePart(headers, getFileContent(in, buffer, status))
          }
        }

        Multipart(parts : _*)

      case line if line.startsWith(new String(status.end)) => Multipart()

      case line => throw new HttpException("Invalid start of mulitpart")
    }
  }

  private def getTextContent(in: InputStream, buffer: Array[Byte], charset: String, status: Status): String = {
    var content = new ArrayBuffer[Byte]

    var length = in.readLine(buffer)
    if (length == -1)
      throw new HttpException("Invalid part: truncation detected")

    while (!buffer.startsWith(status.start)) {
      content ++= buffer.take(length)

      length = in.readLine(buffer)
      if (length == -1)
        throw new HttpException("Invalid part: truncation detected")
    }

    if (buffer.startsWith(status.end))
      status.continue = false

    // Remove trailing crlf
    new String(content.dropRight(2).toArray, charset)
  }

  private def getFileContent(in: InputStream, buffer: Array[Byte], status: Status): File = {
    val content = File.createTempFile("scamper-dest-file-", ".tmp", dest)

    content.withOutputStream { out =>
      var length = in.readLine(buffer)
      if (length == -1)
        throw new HttpException("Invalid part: truncation detected")

      while (!buffer.startsWith(status.start)) {
        out.write(buffer, 0, length)

        length = in.readLine(buffer)
        if (length == -1)
          throw new HttpException("Invalid part: truncation detected")
      }

      if (buffer.startsWith(status.end))
        status.continue = false
    }

    // Remove trailing crlf
    val file = new java.io.RandomAccessFile(content, "rw")
    try file.setLength(content.length - 2)
    finally Try(file.close())

    content
  }
}

