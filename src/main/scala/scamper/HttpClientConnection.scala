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

import ImplicitExtensions._

private class HttpClientConnection private (socket: Socket) extends Closeable {
  private val buffer = new Array[Byte](8192)
  private val headers = new ArrayBuffer[Header](32)

  socket.setSoTimeout(30000)
  socket.setSendBufferSize(8192)
  socket.setReceiveBufferSize(8192)

  def send(request: HttpRequest): HttpResponse = {
    socket.writeLine(request.startLine.toString)
    request.headers.map(_.toString).foreach(socket.writeLine)
    socket.writeLine()

    var len = 0

    if (!request.body.isKnownEmpty) {
      val in = request.body.getInputStream

      while ({ len = in.read(buffer); len != -1 })
        socket.write(buffer, 0, len)
    }

    getResponse()
  }

  private def getResponse(): HttpResponse = {
    val statusLine = StatusLine.parse(socket.readLine(buffer))
    var line = ""

    while ({ line = socket.readLine(buffer); line != "" })
      headers += Header.parse(line)

    HttpResponse(statusLine, headers.toSeq, Entity(() => socket.getInputStream))
  }

  def close(): Unit = socket.close()
}

private object HttpClientConnection {
  private def createSslSocket(host: String, port: Int): Socket =
    SSLSocketFactory.getDefault.createSocket(host, port)

  def apply(host: String, port: Int, secure: Boolean = false): HttpClientConnection = {
    if (secure) new HttpClientConnection(createSslSocket(host, port))
    else new HttpClientConnection(new Socket(host, port))
  }
}
