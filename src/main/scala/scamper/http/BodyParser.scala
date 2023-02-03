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

import scala.collection.mutable.ArrayBuffer

import scamper.http.types.MediaType

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
   * Gets body parser for buffered input stream.
   *
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   *
   * @note The input stream returned from parser is decoded.
   */
  def stream(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[InputStream] =
    InputStreamBodyParser(maxLength, bufferSize)

  /**
   * Gets body parser for buffered reader.
   *
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def reader(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[BufferedReader] =
    BufferedReaderBodyParser(maxLength, bufferSize)

  /**
   * Gets body parser for byte array.
   *
   * @param maxLength maximum length
   * @param bufferSize buffer size in bytes
   *
   * @note The bytes returned from parser are decoded.
   */
  def bytes(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[Array[Byte]] =
    ByteArrayBodyParser(maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for string.
   *
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def string(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[String] =
    StringBodyParser(maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for query string.
   *
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   */
  def query(maxLength: Int = 8388608, bufferSize: Int = 8192): BodyParser[QueryString] =
    QueryStringBodyParser(maxLength.max(0), bufferSize.max(8192))

  /**
   * Gets body parser for file storage.
   *
   * @param dest destination to which message body is stored
   * @param maxLength maximum length in bytes
   * @param bufferSize buffer size in bytes
   *
   * @note If `dest` is a directory, then the parser creates a new file in
   * specified directory on each parsing invocation; otherwise, the parser
   * overwrites specified file on each invocation. In either case, the bytes in
   * file are decoded.
   */
  def file(dest: File = File(sys.props("java.io.tmpdir")), maxLength: Long = 8388608, bufferSize: Int = 8192): BodyParser[File] =
    FileBodyParser(dest, maxLength.max(0), bufferSize.max(8192))

private class InputStreamBodyParser(val maxLength: Long, bufferSize: Int) extends BodyParser[InputStream] with BodyDecoder:
  def parse(message: HttpMessage): InputStream =
    BufferedInputStream(decode(message), bufferSize)

private class BufferedReaderBodyParser(val maxLength: Long, bufferSize: Int) extends BodyParser[BufferedReader] with BodyDecoder:
  def parse(message: HttpMessage): BufferedReader =
    BufferedReader(InputStreamReader(decode(message)), bufferSize)

private class ByteArrayBodyParser(val maxLength: Long, bufferSize: Int) extends BodyParser[Array[Byte]] with BodyDecoder:
  def parse(message: HttpMessage): Array[Byte] =
    val in  = decode(message)
    val out = new ArrayBuffer[Byte](bufferSize)
    val buf = new Array[Byte](bufferSize)
    var len = 0
    var tot = 0

    while { len = in.read(buf); len != -1 } do
      tot += len
      if tot > maxLength then throw EntityTooLarge(maxLength)
      out ++= buf.take(len)

    out.toArray

private class StringBodyParser(maxLength: Int, bufferSize: Int) extends BodyParser[String]:
  private val parser = ByteArrayBodyParser(maxLength, bufferSize)

  def parse(message: HttpMessage): String =
    message.getHeaderValue("Content-Type")
      .map(MediaType(_))
      .flatMap(_.params.get("charset"))
      .orElse(Some("UTF-8"))
      .map(charset => String(parser.parse(message), charset))
      .get

private class QueryStringBodyParser(maxLength: Int, bufferSize: Int) extends BodyParser[QueryString]:
  private val parser = StringBodyParser(maxLength, bufferSize)

  def parse(message: HttpMessage): QueryString =
    QueryString(parser.parse(message))

private class FileBodyParser(dest: File, val maxLength: Long, bufferSize: Int) extends BodyParser[File] with BodyDecoder:
  def parse(message: HttpMessage): File =
    val in       = decode(message)
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

  private def getDestFile(): File =
    dest.isDirectory match
      case true  => File.createTempFile("scamper-dest-file-", ".tmp", dest)
      case false => dest
