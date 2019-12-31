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
package scamper.server

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, EOFException }
import java.net.{ Socket, SocketException, SocketTimeoutException }
import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

import scamper.{ Auxiliary, Uri, Validate }
import scamper.logging.Logger
import scamper.websocket._
import scamper.websocket.Opcode.Registry._
import scamper.websocket.StatusCode.Registry._

private class ServerWebSocketSession private (conn: WebSocketConnection, properties: Map[String, Any]) extends WebSocketSession {
  private type EitherHandler[T] = Either[(T, Boolean) => Any, T => Any]

  private implicit val executor = Auxiliary.executor

  private var _bufferCapacity: Int = Int.MaxValue
  private var _idleTimeout: Int = 0

  private var textHandler: EitherHandler[String] = Left(nullHandler)
  private var binaryHandler: EitherHandler[Array[Byte]] = Left(nullHandler)

  private var pingHandler: Option[Array[Byte] => Any] = None
  private var pongHandler: Option[Array[Byte] => Any] = None

  private var errorHandler: Option[Throwable => Any] = None
  private var closeHandler: Option[StatusCode => Any] = None

  private val closeSent = new AtomicBoolean(false)
  private val closeReceived = new AtomicBoolean(false)

  val id: String = getProperty("session.id")
  val target: Uri = getProperty("session.target")
  val protocolVersion: String = getProperty("session.protocolVersion")
  val logger: Logger = getProperty("session.logger")

  private val tag = s"::websocket::$id"

  try
    Future {
      logger.info(s"$tag - Starting websocket session at $target")

      while (isOpen) {
        val frame = conn.read(idleTimeout)

        if (frame.length > bufferCapacity())
          throw WebSocketError(MessageTooBig)

        if (!frame.isMasked && frame.length > 0)
          throw WebSocketError(ProtocolError)

        val data = getData(frame) 

        frame.opcode match {
          case Continuation => throw WebSocketError(ProtocolError)
          case Text         => doText(data, frame.isFinal)
          case Binary       => doBinary(data, frame.isFinal)
          case Ping         => doPing(data)
          case Pong         => doPong(data)
          case Close        => doClose(data)
        }
      }
    } recover {
      case WebSocketError(statusCode) =>
        close(statusCode)

      case _: SocketTimeoutException =>
        close(GoingAway)

      case err: SocketException =>
        if (!closeSent.get()) {
          doError(err)
          close(AbnormalClosure)
        }

      case err =>
        doError(err)
        close(AbnormalClosure)
    } onComplete {
      case _ => logger.info(s"$tag - Exiting websocket session at $target")
    }
  catch {
    case err: Exception =>
      logger.error(s"$tag - Exiting websocket session at $target", err)
      Try(conn.close())
  }

  def isSecure: Boolean = conn.isSecure
  def isOpen: Boolean = conn.isOpen

  def idleTimeout(): Int = _idleTimeout

  def idleTimeout(milliseconds: Int): this.type = {
    if (milliseconds < 0)
      throw new IllegalArgumentException()
    _idleTimeout = milliseconds
    this
  }

  def bufferCapacity(): Int = _bufferCapacity

  def bufferCapacity(size: Int): this.type = {
    if (size < 0)
      throw new IllegalArgumentException()
    _bufferCapacity = size
    this
  }

  def send(message: String): Unit =
    conn.write(makeFrame(message.getBytes("UTF-8"), Text))

  def sendAsynchronously[T](message: String)(callback: Try[Unit] => T): Unit =
    Future(send(message)).onComplete(callback)

  def send(message: Array[Byte]): Unit =
    conn.write(makeFrame(message, Binary))

  def sendAsynchronously[T](message: Array[Byte])(callback: Try[Unit] => T): Unit =
    Future(send(message)).onComplete(callback)

  def ping(data: Array[Byte] = Array.empty): Unit =
    conn.write(makeFrame(data, Ping))

  def pingAsynchronously[T](data: Array[Byte] = Array.empty)(callback: Try[Unit] => T): Unit =
    Future(ping(data)).onComplete[T](callback)

  def pong(data: Array[Byte] = Array.empty): Unit =
    conn.write(makeFrame(data, Ping))

  def pongAsynchronously[T](data: Array[Byte] = Array.empty)(callback: Try[Unit] => T): Unit =
    Future(pong(data)).onComplete(callback)

  def close(statusCode: StatusCode = NormalClosure): Unit =
    if (closeSent.compareAndSet(false, true)) {
      Try(doClose(statusCode.toData))

      try
        if (statusCode != NoStatusPresent && statusCode != AbnormalClosure && statusCode != TlsHandshakeFailure)
          conn.write(WebSocketFrame(statusCode, None))
      catch {
        case err: Exception => doError(err)
      } finally {
        Try(conn.close())
      }
    }

  def onText[T](handler: String => T): this.type = {
    textHandler = if (handler == null) Left(nullHandler) else Right(handler)
    this
  }

  def onPartialText[T](handler: (String, Boolean) => T): this.type = {
    textHandler = if (handler == null) Left(nullHandler) else Left(handler)
    this
  }

  def onBinary[T](handler: Array[Byte] => T): this.type = {
    binaryHandler = if (handler == null) Left(nullHandler) else Right(handler)
    this
  }

  def onPartialBinary[T](handler: (Array[Byte], Boolean) => T): this.type = {
    binaryHandler = if (handler == null) Left(nullHandler) else Left(handler)
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

  private def doText(data: Array[Byte], last: Boolean): Unit =
    if (last)
      textHandler match {
        case Right(handle) => handle(new String(data, "UTF-8"))
        case Left(handle)  => handle(new String(data, "UTF-8"), true)
      }
    else
      doContinuation(textHandler, data) { data =>
        new String(data, "UTF-8")
      }

  private def doBinary(data: Array[Byte], last: Boolean): Unit =
    if (last)
      binaryHandler match {
        case Right(handle) => handle(data)
        case Left(handle)  => handle(data, true)
      }
    else
      doContinuation(binaryHandler, data)(identity)

  private def doContinuation[T](handler: EitherHandler[T], data: Array[Byte])(encode: Array[Byte] => T): Unit = {
    val buffer = new ByteArrayOutputStream()

    handler match {
      case Right(handle) => buffer.write(data)
      case Left(handle)  => handle(encode(data), false)
    }

    var keepGoing = true

    while (keepGoing) {
      val frame = conn.read(idleTimeout)

      if (frame.length + buffer.size > bufferCapacity())
        throw WebSocketError(MessageTooBig)

      if (!frame.isMasked && frame.length > 0)
        throw WebSocketError(ProtocolError)

      val newData = getData(frame)

      frame.opcode match {
        case Continuation => 
          handler match {
            case Right(handle) => buffer.write(newData)
            case Left(handle)  => handle(encode(newData), frame.isFinal)
          }

          if (frame.isFinal) {
            handler.foreach { handle => handle(encode(buffer.toByteArray)) }
            keepGoing = false
          }

        case Text   => throw WebSocketError(ProtocolError)
        case Binary => throw WebSocketError(ProtocolError)
        case Ping   => doPing(newData)
        case Pong   => doPong(newData)
        case Close  => doClose(newData); keepGoing = false
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
      val statusCode = StatusCode.get(data.take(2)).getOrElse(NoStatusPresent)

      try
        closeHandler.foreach { handle =>
          handle(statusCode)
        }
      finally
        close(statusCode)
    }

  private def getProperty[T](name: String): T =
    try
      Validate.notNull(properties(name).asInstanceOf[T])
    catch {
      case err: Exception =>
        Try(conn.close())
        throw err
    }

  private def nullHandler[T]: (T, Boolean) => Unit = (_, _) => ()

  private def makeFrame(data: Array[Byte], opcode: Opcode): WebSocketFrame =
    WebSocketFrame(
      true,
      opcode,
      None,
      data.size,
      new ByteArrayInputStream(data)
    )

  private def getData(frame: WebSocketFrame): Array[Byte] = {
    if (frame.length > Int.MaxValue)
      throw WebSocketError(MessageTooBig)

    val data = new Array[Byte](frame.length.toInt)
    frame.payload.read(data)
    frame.maskingKey.map { key => mask(key, data) }.getOrElse(data)
  }
}

private object ServerWebSocketSession {
  def apply(socket: Socket, properties: Map[String, Any]): ServerWebSocketSession =
    new ServerWebSocketSession(WebSocketConnection(socket), properties) 
}
