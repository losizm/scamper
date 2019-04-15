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
package scamper.client

import java.io.InputStream
import java.net.Socket

import scala.collection.mutable.ArrayBuffer

import scamper.{ Auxiliary, Compressor, Entity, Header, HttpException, HttpRequest, HttpResponse, StatusLine }
import scamper.RequestMethods.HEAD
import scamper.headers.TransferEncoding
import scamper.types.TransferCoding

import Auxiliary.SocketType

private class HttpClientConnection(socket: Socket) extends AutoCloseable {
  private val buffer = new Array[Byte](8192)

  def send(request: HttpRequest): HttpResponse = {
    socket.writeLine(request.startLine.toString)
    request.headers.map(_.toString).foreach(socket.writeLine)
    socket.writeLine()

    if (!request.body.isKnownEmpty)
      writeBody(request)

    socket.flush()
    getResponse(request.method == HEAD)
  }

  def close(): Unit = socket.close()

  private def writeBody(request: HttpRequest): Unit =
    request.getTransferEncoding.map { encoding =>
      val in = encodeInputStream(request.body.getInputStream, encoding)
      var chunkSize = 0

      while ({ chunkSize = in.read(buffer); chunkSize != -1 }) {
        socket.writeLine(chunkSize.toHexString)
        socket.write(buffer, 0, chunkSize)
        socket.writeLine()
      }

      socket.writeLine("0")
      socket.writeLine()
    }.getOrElse {
      val in = request.body.getInputStream
      var length = 0
      while ({ length = in.read(buffer); length != -1 })
        socket.write(buffer, 0, length)
    }

  private def encodeInputStream(in: InputStream, encoding: Seq[TransferCoding]): InputStream =
    encoding.foldLeft(in) { (in, enc) =>
      if (enc.isChunked) in
      else if (enc.isGzip) Compressor.gzip(in)(Auxiliary.executor)
      else if (enc.isDeflate) Compressor.deflate(in)(Auxiliary.executor)
      else throw new HttpException(s"Unsupported transfer encoding: $enc")
    }

  private def getResponse(headOnly: Boolean): HttpResponse = {
    val statusLine = StatusLine.parse(socket.getLine(buffer))
    val headers = new ArrayBuffer[Header]
    var line = ""

    while ({ line = socket.getLine(buffer); line != "" })
      line.matches("[ \t]+.*") match {
        case true =>
          if (headers.isEmpty) throw new HttpException("Cannot parse response headers")
          val last = headers.last
          headers.update(headers.length - 1, Header(last.name, last.value + " " + line.trim()))
        case false =>
          headers += Header.parse(line)
      }

    HttpResponse(
      statusLine,
      headers.toSeq,
      headOnly match {
        case true  => Entity.empty
        case false => Entity.fromInputStream(socket.getInputStream())
      }
    )
  }
}
