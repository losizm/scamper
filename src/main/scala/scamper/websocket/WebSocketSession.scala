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
package scamper.websocket

import java.net.Socket

import scala.util.Try

import scamper.Uri
import scamper.logging.{ Logger, NullLogger }
import StatusCode.Registry.NormalClosure

/** Defines session for WebSocket connection. */
trait WebSocketSession {
  /** Gets session identifer. */
  def id: String

  /** Gets target of websocket request. */
  def target: Uri

  /** Gets websocket protocol version. */
  def protocolVersion: String

  /** Gets logger associated with session. */
  def logger: Logger

  /** Tests whether websocket is using secure transport. */
  def isSecure: Boolean

  /** Tests whether websocket session is open. */
  def isOpen: Boolean

  /**
   * Gets websocket idle timeout in milliseconds.
   *
   * Timeout of zero disables this option &ndash; i.e., timeout is indefinite.
   *
   * @note If no activity transpires for specified duration, then session is
   *  closed with status code [[StatusCode.Registry.GoingAway GoingAway]].
   */
  def idleTimeout(): Int

  /**
   * Sets websocket idle timeout.
   *
   * Timeout of zero disables this option &ndash; i.e., timeout is indefinite.
   *
   * @param milliseconds idle timeout
   *
   * @note If no activity transpires for specified duration, then session is
   *  closed with status code [[StatusCode.Registry.GoingAway GoingAway]].
   *
   * @return this session
   */
  def idleTimeout(milliseconds: Int): this.type

  /**
   * Gets buffer capacity (in bytes) of message.
   *
   * @note If incoming message or message part exceeds specified capacity, then
   *  session is closed with status code [[StatusCode.Registry.MessageTooBig MessageTooBig]].
   */
  def bufferCapacity(): Int

  /**
   * Sets buffer capacity (in bytes) of message.
   *
   * @param size buffer capacity in bytes
   *
   * @note If incoming message or message part exceeds specified capacity, then
   *  session is closed with status code [[StatusCode.Registry.MessageTooBig MessageTooBig]].
   *
   * @return this session
   */
  def bufferCapacity(size: Int): this.type

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
  def sendAsynchronously[T](message: String)(callback: Try[Unit] => T): Unit

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
  def sendAsynchronously[T](message: Array[Byte])(callback: Try[Unit] => T): Unit

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
  def pingAsynchronously[T](data: Array[Byte] = Array.empty)(callback: Try[Unit] => T): Unit

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
  def pongAsynchronously[T](data: Array[Byte] = Array.empty)(callback: Try[Unit] => T): Unit

  /**
   * Closes session with supplied status code.
   *
   * @param code status code of closure
   */
  def close(code: StatusCode = NormalClosure): Unit

  /**
   * Sets handler for incoming text message.
   *
   * @param handler text message handler
   *
   * @return this session
   */
  def onText[T](handler: String => T): this.type

  /**
   * Sets handler for incoming text message part.
   *
   * The handler accepts a partial message and an indicator specifying if it's
   * the last part.
   *
   * @param handler text message handler
   *
   * @return this session
   */
  def onPartialText[T](handler: (String, Boolean) => T): this.type

  /**
   * Sets handler for incoming binary message.
   *
   * @param handler binary message handler
   *
   * @return this session
   */
  def onBinary[T](handler: Array[Byte] => T): this.type

  /**
   * Sets handler for incoming binary message part.
   *
   * The handler accepts a partial message and an indicator specifying if it's
   * the last part.
   *
   * @param handler binary message handler
   *
   * @return this session
   */
  def onPartialBinary[T](handler: (Array[Byte], Boolean) => T): this.type

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
   * Wraps websocket session around an already established client connection.
   *
   * @param conn websocket connection
   * @param id websocket identifier
   * @param target target URI for which connection was established
   * @param protocolVersion websocket protocol version
   * @param logger optional logger
   */
  def forClient(conn: WebSocketConnection, id: String, target: Uri, protocolVersion: String, logger: Option[Logger]): WebSocketSession =
    new WebSocketSessionImpl(id, target, protocolVersion, logger.getOrElse(NullLogger))(conn, false)

  /**
   * Wraps websocket session around an already established client connection.
   *
   * @param socket socket from which websocket connection is constructed
   * @param id websocket identifier
   * @param target target URI for which connection was established
   * @param protocolVersion websocket protocol version
   * @param logger optional logger
   */
  def forClient(socket: Socket, id: String, target: Uri, protocolVersion: String, logger: Option[Logger]): WebSocketSession =
    forClient(WebSocketConnection(socket), id, target, protocolVersion, logger)

  /**
   * Wraps websocket session around an already established server connection.
   *
   * @param conn websocket connection
   * @param id websocket identifier
   * @param target target URI for which connection was established
   * @param protocolVersion websocket protocol version
   * @param logger optional logger
   */
  def forServer(conn: WebSocketConnection, id: String, target: Uri, protocolVersion: String, logger: Option[Logger]): WebSocketSession =
    new WebSocketSessionImpl(id, target, protocolVersion, logger.getOrElse(NullLogger))(conn, true)

  /**
   * Wraps websocket session around an already established server connection.
   *
   * @param socket socket from which websocket connection is constructed
   * @param id websocket identifier
   * @param target target URI for which connection was established
   * @param protocolVersion websocket protocol version
   * @param logger optional logger
   */
  def forServer(socket: Socket, id: String, target: Uri, protocolVersion: String, logger: Option[Logger]): WebSocketSession =
    forServer(WebSocketConnection(socket), id, target, protocolVersion, logger)
}
