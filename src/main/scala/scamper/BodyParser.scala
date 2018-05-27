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

import java.io.{ File, FileOutputStream, InputStream }

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

import scamper.types._

/**
 * Provides utility for parsing HTTP message body.
 *
 * @see [[BodyParsers]]
 */
trait BodyParser[T] {
  /**
   * Parses body of supplied HTTP message and returns instance of defined type.
   */
  def parse(message: HttpMessage): T
}

/** Provides body parser implementations. */
object BodyParsers {
  /**
   * Gets body parser to collect raw bytes of message body.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def bytes(maxLength: Int = 4 * 1024 * 1024): BodyParser[Array[Byte]] =
    new ByteArrayBodyParser(maxLength)

  /**
   * Gets body parser to collect text content.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def text(maxLength: Int = 4 * 1024 * 1024): BodyParser[String] =
    new TextBodyParser(maxLength)

  /**
   * Gets body parser to collect form data.
   *
   * @param maxLength maximum length in bytes allowed
   */
  def form(maxLength: Int = 4 * 1024 * 1024): BodyParser[Map[String, Seq[String]]] =
    new FormBodyParser(maxLength)

  /**
   * Gets body parser to store message body to file.
   *
   * If {@code dest} is a directory, then the parser creates a new file in the
   * specified directory on each parsing invocation. Otherwise, the parser
   * overwrites the specified file on each invocation.
   *
   * @param dest destination to which message body is stored
   * @param maxLength maximum length in bytes allowed
   */
  def file(dest: File = new File(sys.props("java.io.tmpdir")), maxLength: Long = 4 * 1024 * 1024): BodyParser[File] =
    new FileBodyParser(dest, maxLength, maxLength.min(Int.MaxValue).toInt)
}

private class ByteArrayBodyParser(val maxLength: Long) extends BodyParser[Array[Byte]] with BodyParsing {
  val maxBufferSize = maxLength.toInt
  private val bufferSize = maxBufferSize.min(8192)

  def parse(message: HttpMessage): Array[Byte] =
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
  private val bodyParser = new ByteArrayBodyParser(maxLength)

  def parse(message: HttpMessage): String =
    message.getHeaderValue("Content-Type")
      .map(MediaType.apply)
      .flatMap(_.params.get("charset"))
      .orElse(Some("UTF-8"))
      .map(new String(bodyParser.parse(message), _)).get
}

private class FormBodyParser(maxLength: Int) extends BodyParser[Map[String, Seq[String]]] {
  private val bodyParser = new TextBodyParser(maxLength)

  def parse(message: HttpMessage): Map[String, Seq[String]] =
    QueryParams.parse(bodyParser.parse(message))
}

private class FileBodyParser(dest: File, val maxLength: Long, val maxBufferSize: Int) extends BodyParser[File] with BodyParsing {
  def parse(message: HttpMessage): File =
    withInputStream(message) { in =>
      val destFile = getDestFile()
      val out = new FileOutputStream(destFile)

      try {
        val buf = new Array[Byte](maxBufferSize.min(8192))
        var len = 0

        while ({ len = in.read(buf); len != -1 })
          out.write(buf, 0, len)

        destFile
      } finally Try(out.close())
    }

  private def getDestFile(): File =
    if (dest.isDirectory) File.createTempFile("scamper-dest-file-", ".tmp", dest)
    else dest
}
