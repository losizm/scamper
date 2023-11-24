/*
 * Copyright 2023 Carlos Conyers
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
package server

import java.io.{ EOFException, InputStream }
import java.net.{ Socket, URISyntaxException }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

import scamper.http.headers.toTransferEncoding
import scamper.http.types.TransferCoding

import ResponseStatus.Registry.*

private given toHttpServerSocket: Conversion[Socket, HttpServerSocket] = HttpServerSocket(_)

private class HttpServerSocket(socket: Socket) extends AnyVal:
  def readHttpRequest(firstByte: Byte, bufferSize: Int, headerLimit: Int): HttpRequest =
    val buffer = new Array[Byte](bufferSize)

    buffer(0) = firstByte

    val method    = readMethod(buffer, 1)
    val target    = readTarget(buffer)
    val version   = readVersion(buffer)
    val startLine = RequestLine(method, target, version)
    val headers   = readHeaders(buffer, headerLimit)

    HttpRequest(startLine, headers, Entity(socket.getInputStream))

  def writeHttpResponse(res: HttpResponse, bufferSize: Int)(using executor: ExecutionContext): Unit =
    socket.writeLine(res.startLine.toString)
    res.headers.foreach(header => socket.writeLine(header.toString))
    socket.writeLine()

    if !res.body.isKnownEmpty then
      val buffer = new Array[Byte](bufferSize)

      res.transferEncodingOption.map { encoding =>
        val in = encode(res.body.data, encoding, bufferSize)
        var chunkSize = in.read(buffer)
        while chunkSize != -1 do
          socket.writeLine(chunkSize.toHexString)
          socket.write(buffer, 0, chunkSize)
          socket.writeLine()
          chunkSize = in.read(buffer)
        socket.writeLine("0")
        socket.writeLine()
      }.getOrElse {
        socket.getOutputStream.write(res.body.data, buffer)
      }

    socket.flush()

  private def readMethod(buffer: Array[Byte], offset: Int): RequestMethod =
    try RequestMethod(socket.getToken(" ", buffer, offset)) catch
      case _: IndexOutOfBoundsException => throw ReadError(NotImplemented)
      case _: IllegalArgumentException  => throw ReadError(BadRequest)

  private def readTarget(buffer: Array[Byte]): Uri =
    try Uri(socket.getToken(" ", buffer)) catch
      case _: IndexOutOfBoundsException => throw ReadError(UriTooLong)
      case _: URISyntaxException        => throw ReadError(BadRequest)

  private def readVersion(buffer: Array[Byte]): HttpVersion =
    try HttpVersion(socket.getLine(buffer)) catch
      case _: IndexOutOfBoundsException => throw ReadError(BadRequest)
      case _: IllegalArgumentException  => throw ReadError(BadRequest)

  private def readHeaders(buffer: Array[Byte], headerLimit: Int): Seq[Header] =
    try
      val headers   = new ArrayBuffer[Header]
      val readLimit = headerLimit * buffer.size
      var readSize  = 0
      var line      = socket.getLine(buffer)

      while line != "" do
        readSize += line.size

        if readSize <= readLimit then
          line.matches("[ \t]+.*") match
            case true =>
              if headers.isEmpty then throw ReadError(BadRequest)
              val last = headers.last
              headers.update(headers.size - 1, Header(last.name, last.value + " " + line.trim()))
            case false =>
              headers.size < headerLimit match
                case true  => headers += Header(line)
                case false => throw ReadError(RequestHeaderFieldsTooLarge)
          line = socket.getLine(buffer)
        else
          throw ReadError(RequestHeaderFieldsTooLarge)

      headers.toSeq
    catch case _: IndexOutOfBoundsException =>
      throw ReadError(RequestHeaderFieldsTooLarge)

  private def encode(in: InputStream, encoding: Seq[TransferCoding], bufferSize: Int)(using executor: ExecutionContext): InputStream =
    encoding.foldLeft(in) { (in, enc) =>
      if      enc.isChunked then in
      else if enc.isGzip    then Compressor.gzip(in, bufferSize)
      else if enc.isDeflate then Compressor.deflate(in, bufferSize)
      else                  throw HttpException(s"Unsupported transfer encoding: $enc")
    }
