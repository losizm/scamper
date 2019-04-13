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

import java.io.{ File, FileOutputStream }

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

import scamper.types.MediaType

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
   * Gets body parser for collecting raw bytes in message body.
   *
   * @param maxLength maximum length
   */
  def bytes(maxLength: Int = 8 * 1024 * 1024): BodyParser[Array[Byte]] =
    new ByteArrayBodyParser(maxLength.max(0))

  /**
   * Gets body parser for collecting text in message body.
   *
   * @param maxLength maximum length in bytes
   */
  def text(maxLength: Int = 8 * 1024 * 1024): BodyParser[String] =
    new TextBodyParser(maxLength.max(0))

  /**
   * Gets body parser for collecting form data in message body.
   *
   * @param maxLength maximum length in bytes
   */
  def form(maxLength: Int = 8 * 1024 * 1024): BodyParser[Map[String, Seq[String]]] =
    new FormBodyParser(maxLength.max(0))

  /**
   * Gets body parser for collecting form data (as query) in message body.
   *
   * @param maxLength maximum length in bytes
   */
  def query(maxLength: Int = 8 * 1024 * 1024): BodyParser[QueryString] =
    new QueryBodyParser(maxLength.max(0))

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
  def file(dest: File = new File(sys.props("java.io.tmpdir")), maxLength: Long = 8 * 1024 * 1024, bufferSize: Int = 8192): BodyParser[File] =
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
      val out = new FileOutputStream(destFile)

      try {
        val buffer = new Array[Byte](bufferSize)
        var length = 0
        while ({ length = in.read(buffer); length != -1 })
          out.write(buffer, 0, length)
        destFile
      } finally Try(out.close())
    }

  private def getDestFile(): File =
    dest.isDirectory match {
      case true  => File.createTempFile("scamper-dest-file-", ".tmp", dest)
      case false => dest
    }
}
