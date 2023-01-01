package scamper
package http
package client

import java.io.{ File, InputStream }
import java.net.{ ServerSocket, Socket, SocketException }

import scala.collection.mutable.ListBuffer
import scala.util.{ Failure, Success, Try }

import types.MediaType

class NoContentLengthServer(data: File):
  private val server      = ServerSocket(0)
  private val contentType = MediaType.forFile(data).getOrElse(MediaType.octetStream)

  def port: Int =
    server.getLocalPort()

  def open(): Unit =
    new Thread() {
      override def run(): Unit =
        try
          while !server.isClosed do
            process()
        catch case err: Exception =>
          if !server.isClosed then
            throw err
        finally
          close()
    }.start()

  def close(): Unit =
    new Thread() {
      override def run(): Unit =
        Thread.sleep(2000)
        Try(server.close())
    }.start()

  private def process(): Unit =
    val client = server.accept()

    try
      receive(client)
      send(client)
    finally
      Try(client.close())

  private def receive(socket: Socket): Seq[String] =
    val input  = new ListBuffer[String]
    val buffer = new Array[Byte](8192)
    var line   = ""

    while { line = socket.getLine(buffer); line.nonEmpty } do
      input += line

    input.toSeq

  private def send(socket: Socket): Unit =
    val buffer = new Array[Byte](8192)
    var length = 0

    socket.writeLine("HTTP/1.1 200 OK")
    socket.writeLine(s"Content-Type: $contentType")
    socket.writeLine()

    data.withInputStream { in =>
      while { length = in.read(buffer); length != -1 } do
        socket.write(buffer, 0, length)
    }

    socket.flush()
