/*
 * Copyright 2017-2020 Carlos Conyers
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
package scamper.websocket

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, EOFException, InputStream }
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.Future
import scala.util.Try

import scamper.{ Auxiliary, HttpRequest, Uri }
import scamper.logging.Logger
import scamper.websocket.Opcode.Registry._
import scamper.websocket.StatusCode.Registry._

import Auxiliary.InputStreamType

private[scamper] class WebSocketSessionImpl(val id: String, val target: Uri, val protocolVersion: String, val logger: Logger)
    (conn: WebSocketConnection, serverMode: Boolean, deflate: DeflateMode, request: Option[HttpRequest] = None) extends WebSocketSession {

  private var _idleTimeout: Int = 0
  private var _payloadLimit: Int = 64 * 1024
  private var _bufferCapacity: Int = 8 * 1024 * 1024

  private var textHandler: Option[String => Any] = None
  private var binaryHandler: Option[Array[Byte] => Any] = None
  private var pingHandler: Option[Array[Byte] => Any] = None
  private var pongHandler: Option[Array[Byte] => Any] = None
  private var errorHandler: Option[Throwable => Any] = None
  private var closeHandler: Option[StatusCode => Any] = None

  private val openInvoked = new AtomicBoolean(false)
  private val closeSent = new AtomicBoolean(false)
  private val closeReceived = new AtomicBoolean(false)

  private implicit val executor = Auxiliary.executor

  def isSecure: Boolean = conn.isSecure

  def state: SessionState =
    conn.isOpen match {
      case true  =>
        openInvoked.get match {
          case true  => SessionState.Open
          case false => SessionState.Pending
        }
      case false => SessionState.Closed
    }

  def idleTimeout: Int = _idleTimeout

  def idleTimeout(milliseconds: Int): this.type = {
    _idleTimeout = milliseconds.max(0)
    this
  }

  def payloadLimit: Int = _payloadLimit

  def payloadLimit(length: Int): this.type = {
    _payloadLimit = length.max(1024)
    this
  }

  def bufferCapacity: Int = _bufferCapacity

  def bufferCapacity(length: Int): this.type = {
    _bufferCapacity = length.max(8192)
    this
  }

  def open(): Unit =
    if (openInvoked.compareAndSet(false, true))
      start()

  def close(statusCode: StatusCode = NormalClosure): Unit =
    if (closeSent.compareAndSet(false, true)) {
      Try(doClose(statusCode.toData))

      try
        if (!statusCode.reserved)
          conn.write(makeFrame(statusCode.toData, Close))
      catch {
        case err: Exception => if (!closeReceived.get) doError(err)
      } finally {
        Try(conn.close())
      }
    }

  def send(message: String): Unit =
    sendData(message.getBytes("UTF-8"), false)

  def sendAsync[T](message: String)(callback: Try[Unit] => T): Unit =
    Future(send(message)).onComplete(callback)

  def send(message: Array[Byte]): Unit =
    sendData(message, true)

  def sendAsync[T](message: Array[Byte])(callback: Try[Unit] => T): Unit =
    Future(send(message)).onComplete(callback)

  def send(message: InputStream, binary: Boolean = false): Unit =
    sendData(message, binary)

  def sendAsync[T](message: InputStream, binary: Boolean = false)(callback: Try[Unit] => T): Unit =
    Future(send(message, binary)).onComplete(callback)

  def ping(data: Array[Byte] = Array.empty): Unit = {
    require(data.size <= 125, "data length must not exceed 125 bytes")
    conn.write(makeFrame(data, Ping))
  }

  def pingAsync[T](data: Array[Byte] = Array.empty)(callback: Try[Unit] => T): Unit =
    Future(ping(data)).onComplete(callback)

  def pong(data: Array[Byte] = Array.empty): Unit = {
    require(data.size <= 125, "data length must not exceed 125 bytes")
    conn.write(makeFrame(data, Pong))
  }

  def pongAsync[T](data: Array[Byte] = Array.empty)(callback: Try[Unit] => T): Unit =
    Future(pong(data)).onComplete(callback)

  def onText[T](handler: String => T): this.type = {
    textHandler = Option(handler)
    this
  }

  def onBinary[T](handler: Array[Byte] => T): this.type = {
    binaryHandler = Option(handler)
    this
  }

  def onPing[T](handler: Array[Byte] => T): this.type = {
    pingHandler = Option(handler)
    this
  }

  def onPong[T](handler: Array[Byte] => T): this.type = {
    pongHandler = Option(handler)
    this
  }

  def onError[T](handler: Throwable => T): this.type = {
    errorHandler = Option(handler)
    this
  }

  def onClose[T](handler: StatusCode => T): this.type = {
    closeHandler = Option(handler)
    this
  }

  private[scamper] def getRequest(): HttpRequest = request.get

  private def start(): Unit =
    Future {
      while (state == SessionState.Open) {
        val frame = conn.read(idleTimeout)

        checkFrame(frame)

        val data = getData(frame)

        frame.opcode match {
          case Continuation => throw WebSocketError(ProtocolError)
          case Text         => doText(data, frame.isFinal, frame.isCompressed)
          case Binary       => doBinary(data, frame.isFinal, frame.isCompressed)
          case Ping         => doPing(data)
          case Pong         => doPong(data)
          case Close        => doClose(data)
        }
      }
    } recover {
      case _: SocketTimeoutException =>
        close(GoingAway)

      case err: WebSocketError =>
        if (!closeSent.get()) {
          doError(err)
          close(err.statusCode)
        }

      case err =>
        if (!closeSent.get()) {
          doError(err)
          close(AbnormalClosure)
        }
    }

  private def doText(data: Array[Byte], last: Boolean, compressed: Boolean): Unit =
    if (last)
      textHandler.foreach { handle =>
        compressed match {
          case true  => handle(new String(PermessageDeflate.decompress(data), "UTF-8"))
          case false => handle(new String(data, "UTF-8"))
        }
      }
    else
      doContinuation(textHandler, data, compressed) { data =>
        new String(data, "UTF-8")
      }

  private def doBinary(data: Array[Byte], last: Boolean, compressed: Boolean): Unit =
    if (last)
      binaryHandler.foreach { handle =>
        compressed match {
          case true  => handle(PermessageDeflate.decompress(data))
          case false => handle(data)
        }
      }
    else
      doContinuation(binaryHandler, data, compressed)(identity)

  private def doContinuation[T](handler: Option[T => Any], data: Array[Byte], compressed: Boolean)(decode: Array[Byte] => T): Unit = {
    val buffer = compressed match {
      case true  => new InflaterBuffer
      case false => new IdentityBuffer
    }

    buffer.add(data)

    var continue = true

    while (continue) {
      val frame = conn.read(idleTimeout)

      checkFrame(frame, buffer.size)

      val moreData = getData(frame)

      frame.opcode match {
        case Continuation =>
          buffer.add(moreData)

          if (frame.isFinal) {
            handler.foreach(handle => handle(decode(buffer.get)))
            continue = false
          }

        case Text   => throw WebSocketError(ProtocolError)
        case Binary => throw WebSocketError(ProtocolError)
        case Ping   => doPing(moreData)
        case Pong   => doPong(moreData)
        case Close  => doClose(moreData); continue = false
      }
    }
  }

  private def doPing(data: Array[Byte]): Unit =
    pingHandler.foreach(handle => handle(data))

  private def doPong(data: Array[Byte]): Unit =
    pongHandler.foreach(handle => handle(data))

  private def doError(err: Throwable): Unit =
    errorHandler.foreach(handle => handle(err))

  private def doClose(data: Array[Byte]): Unit =
    if (closeReceived.compareAndSet(false, true)) {
      val statusCode = StatusCode.get(data.take(2)).getOrElse(NoStatusReceived)

      try
        closeHandler.foreach { handle =>
          handle(statusCode)
        }
      finally
        close(statusCode)
    }

  private def sendData(data: Array[Byte], binary: Boolean): Unit =
    (deflate == DeflateMode.None && data.length <= payloadLimit) match {
      case true  =>
        val opcode = if (binary) Binary else Text
        val frame = makeFrame(data, opcode)

        synchronized(conn.write(frame))

      case false =>
        sendData(new ByteArrayInputStream(data), binary)
    }

  private def sendData(data: InputStream, binary: Boolean): Unit = synchronized {
    val in = (deflate == DeflateMode.Message) match {
      case true  => PermessageDeflate.compress(data)
      case false => data
    }

    val buf = new Array[Byte](payloadLimit)
    var len = in.readMostly(buf)

    (deflate == DeflateMode.Frame) match {
      case true =>
        val payload = PermessageDeflate.compress(buf, 0, len)
        conn.write(makeFrame(payload, payload.size, if (binary) Binary else Text, false, true))

      case false =>
        conn.write(makeFrame(buf, len, if (binary) Binary else Text, false, deflate != DeflateMode.None))
    }

    while ({ len = in.readMostly(buf); len != -1 }) {
      (deflate == DeflateMode.Frame) match {
        case true =>
          val payload = PermessageDeflate.compress(buf, 0, len)
          conn.write(makeFrame(payload, payload.size, Continuation, false, true))

        case false =>
          conn.write(makeFrame(buf, len, Continuation, false, false))
      }
    }

    conn.write(makeFrame(buf, 0, Continuation, true, false))
  }

  private def makeFrame(data: Array[Byte], opcode: Opcode): WebSocketFrame =
    makeFrame(data, data.size, opcode, true, false)

  private def makeFrame(data: Array[Byte], length: Int, opcode: Opcode, isFinal: Boolean, isCompressed: Boolean): WebSocketFrame =
    WebSocketFrame(
      isFinal,
      isCompressed,
      opcode,
      serverMode match {
        case true  => None
        case false => Some(MaskingKey())
      },
      length,
      data
    )

  private def checkFrame(frame: WebSocketFrame, bufferSize: Int = 0): Unit = {
    if ((bufferSize + frame.length) > bufferCapacity)
      throw WebSocketError(MessageTooBig)

    frame.isMasked match {
      case true =>
        if (!serverMode)
          throw WebSocketError(ProtocolError)

      case false =>
        if (serverMode)
          throw WebSocketError(ProtocolError)
    }

    if (frame.isCompressed && deflate == DeflateMode.None)
      throw WebSocketError(ProtocolError)
  }

  private def getData(frame: WebSocketFrame): Array[Byte] =
    frame.length.toInt match {
      case 0      => Array.empty
      case length =>
        val data = new Array[Byte](length)
        var position = 0

        while (position < length) {
          val count = frame.payload.read(data, position, length - position)

          if (count == -1)
            throw new EOFException(s"Truncation dectected: Payload length ($position) is less than declared length ($length)")

          position += count
        }

        frame.key.map(key => key(data))
        data
    }
}
