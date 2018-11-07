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

import java.io.Closeable
import java.net.Socket

import javax.net.ssl.SSLSocketFactory

import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import scala.util.control.NonFatal

import ImplicitHeaders.TransferEncoding
import ImplicitExtensions.HttpSocketType

private class HttpClientConnection private (socket: Socket) extends Closeable {
  private val buffer = new Array[Byte](8192)

  def send(request: HttpRequest): HttpResponse = {
    socket.writeLine(request.startLine.toString)
    request.headers.map(_.toString).foreach(socket.writeLine)
    socket.writeLine()

    if (! request.body.isKnownEmpty)
      writeBody(request)

    socket.flush()
    getResponse()
  }

  def close(): Unit = socket.close()

  private def writeBody(request: HttpRequest): Unit =
    request.getTransferEncoding.map { encoding =>
      val in = request.body.getInputStream
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

  private def getResponse(): HttpResponse = {
    val statusLine = StatusLine.parse(socket.readLine(buffer))
    val headers = new ArrayBuffer[Header](8)
    var line = ""

    while ({ line = socket.readLine(buffer); line != "" })
      headers += Header.parse(line)

    HttpResponse(statusLine, headers.toSeq, Entity(() => socket.getInputStream))
  }
}

private object HttpClientConnection {
  private def createSslSocket(host: String, port: Int): Socket =
    SSLSocketFactory.getDefault.createSocket(host, port)

  def apply(host: String, port: Int, secure: Boolean, timeout: Int, bufferSize: Int): HttpClientConnection = {
    val socket = if (secure) createSslSocket(host, port) else new Socket(host, port)

    try {
      socket.setSoTimeout(timeout)
      socket.setSendBufferSize(bufferSize)
      socket.setReceiveBufferSize(bufferSize)
    } catch {
      case NonFatal(cause) =>
        Try(socket.close())
        throw cause
    }

    new HttpClientConnection(socket)
  }
}
