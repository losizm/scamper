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

import java.io.{ File, InputStream }

import scala.collection.mutable.{ ArrayBuffer, ListBuffer }
import scala.util.Try

import scamper.http.headers.ContentType
import scamper.http.types.{ DispositionType, MediaType }

import Auxiliary.{ FileType, InputStreamType }

private class MultipartBodyParser(dest: File, val maxLength: Long, bufferSize: Int) extends BodyParser[Multipart] with BodyDecoder:
  private class Status(val boundary: String):
    val start = ("--" + boundary).getBytes("UTF-8")
    val end = ("--" + boundary + "--").getBytes("UTF-8")
    var continue = true

  def parse(message: HttpMessage): Multipart =
    val mediaType = message.contentType

    (mediaType.isMultipart && mediaType.subtypeName == "form-data") match
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
          }.getOrElse(MediaType.plain)

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

