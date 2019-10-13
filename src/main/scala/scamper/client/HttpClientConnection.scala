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
package scamper.client

import java.io.InputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.Future

import scamper.{ Auxiliary, Compressor, Entity, Header, HeaderStream, HttpException, HttpRequest, HttpResponse, StatusLine }
import scamper.RequestMethod.Registry.HEAD
import scamper.ResponseStatus.Registry.Continue
import scamper.headers.TransferEncoding
import scamper.types.TransferCoding

import Auxiliary.SocketType

private class HttpClientConnection(socket: Socket, bufferSize: Int, continueTimeout: Int) extends AutoCloseable {
  def send(request: HttpRequest): HttpResponse = {
    socket.writeLine(request.startLine.toString)
    request.headers.map(_.toString).foreach(socket.writeLine)
    socket.writeLine()
    socket.flush()

    val continue = new AtomicBoolean(true)

    if (!request.body.isKnownEmpty)
      Future {
        if (request.getHeaderValues("Expect").exists { _.toLowerCase == "100-continue" })
          continue.synchronized { continue.wait(continueTimeout) }

        if (continue.get)
          writeBody(request)
      }(Auxiliary.executor)

    getResponse(request.method == HEAD) match {
      case res if res.status == Continue => getResponse(request.method == HEAD)
      case res                           =>
        if (!res.status.isSuccessful) {
          continue.set(false)
          continue.synchronized { continue.notify() }
        }
        res
    }
  }

  def close(): Unit = socket.close()

  private def writeBody(request: HttpRequest): Unit =
    request.getTransferEncoding.map { encoding =>
      val buffer = new Array[Byte](bufferSize)
      val in = encodeInputStream(request.body.getInputStream, encoding)
      var chunkSize = 0

      while ({ chunkSize = in.read(buffer); chunkSize != -1 }) {
        socket.writeLine(chunkSize.toHexString)
        socket.write(buffer, 0, chunkSize)
        socket.writeLine()
      }

      socket.writeLine("0")
      socket.writeLine()
      socket.flush()
    }.getOrElse {
      val buffer = new Array[Byte](bufferSize)
      val in = request.body.getInputStream
      var length = 0
      while ({ length = in.read(buffer); length != -1 })
        socket.write(buffer, 0, length)
      socket.flush()
    }

  private def encodeInputStream(in: InputStream, encoding: Seq[TransferCoding]): InputStream =
    encoding.foldLeft(in) { (in, enc) =>
      if (enc.isChunked) in
      else if (enc.isGzip) Compressor.gzip(in)(Auxiliary.executor)
      else if (enc.isDeflate) Compressor.deflate(in)(Auxiliary.executor)
      else throw new HttpException(s"Unsupported transfer encoding: $enc")
    }

  private def getResponse(headOnly: Boolean): HttpResponse = {
    val buffer = new Array[Byte](bufferSize)
    val statusLine = StatusLine.parse(socket.getLine(buffer))
    val headers = HeaderStream.getHeaders(socket.getInputStream, buffer)

    HttpResponse(
      statusLine,
      headers,
      headOnly match {
        case true  => Entity.empty
        case false => Entity.fromInputStream(socket.getInputStream())
      }
    )
  }
}
