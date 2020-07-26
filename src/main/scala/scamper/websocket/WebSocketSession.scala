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

import java.io.InputStream
import java.net.Socket

import scala.util.Try

import scamper.{ Entity, HttpRequest, Uri }
import scamper.logging.{ Logger, NullLogger }
import StatusCode.Registry.NormalClosure

/** Defines session for WebSocket connection. */
trait WebSocketSession {
  /** Gets session identifer. */
  def id: String

  /** Gets target of WebSocket request. */
  def target: Uri

  /** Gets WebSocket protocol version. */
  def protocolVersion: String

  /** Gets logger associated with session. */
  def logger: Logger

  /** Tests for secure WebSocket session. */
  def isSecure: Boolean

  /**
   * Gets current state of WebSocket session.
   *
   * === Ready State ===
   *
   * If session is [[ReadyState.Pending Pending]], it must be opened before
   * incoming messages are read.
   *
   * If session is [[ReadyState.Open Open]], it reads each incoming message
   * and passes it to registered message handler.
   *
   * If session is [[ReadyState.Closed Closed]], it can neither receive nor send
   * messages.
   */
  def state: ReadyState

  /**
   * Gets WebSocket idle timeout in milliseconds.
   *
   * Timeout of zero disables this option &ndash; i.e., timeout is indefinite.
   *
   * @note If no activity transpires for specified duration, then session is
   *  closed with status code [[StatusCode.Registry.GoingAway GoingAway]].
   */
  def idleTimeout: Int

  /**
   * Sets WebSocket idle timeout.
   *
   * Timeout of zero disables this option &ndash; i.e., timeout is indefinite.
   *
   * @param milliseconds idle timeout
   *
   * @return this session
   *
   * @note If no activity transpires for specified duration, then session is
   *  closed with status code [[StatusCode.Registry.GoingAway GoingAway]].
   */
  def idleTimeout(milliseconds: Int): this.type

  /**
   * Gets payload limit of outgoing message.
   *
   * @note If outgoing message exceeds specified limit, then the message is
   * sent over multiple frames.
   */
  def payloadLimit: Int

  /**
   * Gets payload limit of outgoing message.
   *
   * @param length payload limit (in bytes)
   *
   * @return this session
   *
   * @note If outgoing message exceeds specified limit, then the message is
   * sent over multiple frames.
   */
  def payloadLimit(length: Int): this.type

  /**
   * Gets buffer capacity of incoming message.
   *
   * @note If message buffer exceeds specified capacity, then session is closed
   * with status code [[StatusCode.Registry.MessageTooBig MessageTooBig]].
   */
  def bufferCapacity: Int

  /**
   * Sets buffer capacity of incoming message.
   *
   * @param length buffer capacity (in bytes)
   *
   * @return this session
   *
   * @note If message buffer exceeds specified capacity, then session is closed
   * with status code [[StatusCode.Registry.MessageTooBig MessageTooBig]].
   */
  def bufferCapacity(length: Int): this.type

  /**
   * Opens session.
   *
   * @note This method becomes an effective no-op if invoked more than once.
   */
  def open(): Unit

  /**
   * Closes session with supplied status code.
   *
   * @param code status code of closure
   */
  def close(code: StatusCode = NormalClosure): Unit

  /**
   * Sends text message.
   *
   * @param message text message
   */
  def send(message: String): Unit

  /**
   * Sends text message asynchronously and on completion passes result to
   * supplied callback.
   *
   * @param message text message
   * @param callback result handler
   */
  def sendAsync[T](message: String)(callback: Try[Unit] => T): Unit

  /**
   * Sends binary message.
   *
   * @param message binary message
   */
  def send(message: Array[Byte]): Unit

  /**
   * Sends binary message asynchronously and on completion passes result to
   * supplied callback.
   *
   * @param message binary message
   * @param callback result handler
   */
  def sendAsync[T](message: Array[Byte])(callback: Try[Unit] => T): Unit

  /**
   * Sends message.
   *
   * If `binary` is `true`, then binary message is sent; otherwise, text message
   * is sent.
   *
   * @param message input stream to message
   * @param binary indicator for binary message
   */
  def send(message: InputStream, binary: Boolean = false): Unit

  /**
   * Sends message asynchronously and on completion passes result to supplied
   * callback.
   *
   * If `binary` is `true`, then binary message is sent; otherwise, text message
   * is sent.
   *
   * @param message input stream to message
   * @param binary indicator for binary message
   * @param callback result handler
   */
  def sendAsync[T](message: InputStream, binary: Boolean = false)(callback: Try[Unit] => T): Unit

  /**
   * Sends ping message.
   *
   * @param data application data to accompany ping message
   */
  def ping(data: Array[Byte] = Array.empty): Unit

  /**
   * Sends ping message asynchronously and on completion passes result to
   * supplied callback.
   *
   * @param data application data to accompany ping message
   * @param callback result handler
   */
  def pingAsync[T](data: Array[Byte] = Array.empty)(callback: Try[Unit] => T): Unit

  /**
   * Sends pong message.
   *
   * @param data application data to accompany pong message
   */
  def pong(data: Array[Byte] = Array.empty): Unit

  /**
   * Sends pong message asynchronously and on completion passes result to
   * supplied callback.
   *
   * @param data application data to accompany pong message
   * @param callback result handler
   */
  def pongAsync[T](data: Array[Byte] = Array.empty)(callback: Try[Unit] => T): Unit

  /**
   * Sets handler for incoming text message.
   *
   * @param handler text message handler
   *
   * @return this session
   */
  def onText[T](handler: String => T): this.type

  /**
   * Sets handler for incoming binary message.
   *
   * @param handler binary message handler
   *
   * @return this session
   */
  def onBinary[T](handler: Array[Byte] => T): this.type

  /**
   * Sets handler for incoming ping message.
   *
   * @param handler ping message handler
   *
   * @return this session
   */
  def onPing[T](handler: Array[Byte] => T): this.type

  /**
   * Sets handler for incoming pong message.
   *
   * @param handler pong message handler
   *
   * @return this session
   */
  def onPong[T](handler: Array[Byte] => T): this.type

  /**
   * Sets handler to be notified when session error occurs.
   *
   * @param handler error handler
   *
   * @return this session
   */
  def onError[T](handler: Throwable => T): this.type

  /**
   * Sets handler to be notified when session closes.
   *
   * @param handler close handler
   *
   * @return this session
   */
  def onClose[T](handler: StatusCode => T): this.type
}

/** Provides factory methods for `WebSocketSession`. */
object WebSocketSession {
  /**
   * Wraps WebSocket session around an already established client connection.
   *
   * @param conn WebSocket connection
   * @param id WebSocket identifier
   * @param target target URI for which connection was established
   * @param protocolVersion WebSocket protocol version
   * @param logger optional logger
   */
  def forClient(conn: WebSocketConnection, id: String, target: Uri, protocolVersion: String, logger: Option[Logger]): WebSocketSession =
    new WebSocketSessionImpl(id, target, protocolVersion, logger.getOrElse(NullLogger))(conn, false)

  /**
   * Wraps WebSocket session around an already established client connection.
   *
   * @param socket socket from which WebSocket connection is constructed
   * @param id WebSocket identifier
   * @param target target URI for which connection was established
   * @param protocolVersion WebSocket protocol version
   * @param logger optional logger
   */
  def forClient(socket: Socket, id: String, target: Uri, protocolVersion: String, logger: Option[Logger]): WebSocketSession =
    forClient(WebSocketConnection(socket), id, target, protocolVersion, logger)

  /**
   * Wraps WebSocket session around an already established server connection.
   *
   * @param conn WebSocket connection
   * @param id WebSocket identifier
   * @param target target URI for which connection was established
   * @param protocolVersion WebSocket protocol version
   * @param logger optional logger
   */
  def forServer(conn: WebSocketConnection, id: String, target: Uri, protocolVersion: String, logger: Option[Logger]): WebSocketSession =
    new WebSocketSessionImpl(id, target, protocolVersion, logger.getOrElse(NullLogger))(conn, true)

  /**
   * Wraps WebSocket session around an already established server connection.
   *
   * @param socket socket from which WebSocket connection is constructed
   * @param id WebSocket identifier
   * @param target target URI for which connection was established
   * @param protocolVersion WebSocket protocol version
   * @param logger optional logger
   */
  def forServer(socket: Socket, id: String, target: Uri, protocolVersion: String, logger: Option[Logger]): WebSocketSession =
    forServer(WebSocketConnection(socket), id, target, protocolVersion, logger)

  private[scamper] def forServer(req: HttpRequest): WebSocketSession = {
    val conn = WebSocketConnection(req.getAttribute[Socket]("scamper.server.message.socket").get)
    val id = req.getAttribute[String]("scamper.server.message.correlate").get
    val target = req.target
    val protocolVersion = req.secWebSocketVersion
    val logger = new SessionLogger(id, req.getAttribute[Logger]("scamper.server.message.logger").get)

    new WebSocketSessionImpl(id, target, protocolVersion, logger)(conn, true, Some(req))
  }
}
