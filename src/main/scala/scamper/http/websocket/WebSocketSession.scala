/*
 * Copyright 2021 Carlos Conyers
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
package websocket

import java.io.{ InputStream, Reader }
import java.net.Socket

import scala.concurrent.Future
import scala.util.Try

import StatusCode.Registry.NormalClosure

/** Defines session for WebSocket connection. */
trait WebSocketSession:
  /** Gets session identifer. */
  def id: String

  /** Gets target of WebSocket request. */
  def target: Uri

  /** Gets WebSocket protocol version. */
  def protocolVersion: String

  /** Tests for secure WebSocket session. */
  def isSecure: Boolean

  /** Gets current state of WebSocket session. */
  def state: SessionState

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
   * @note If outgoing message exceeds specified limit, then message is sent
   * over multiple frames.
   */
  def payloadLimit: Int

  /**
   * Gets payload limit of outgoing message.
   *
   * @param length payload limit in bytes
   *
   * @return this session
   *
   * @note If outgoing message exceeds specified limit, then message is sent
   * over multiple frames.
   */
  def payloadLimit(length: Int): this.type

  /**
   * Gets capacity of incoming message.
   *
   * @note If message exceeds specified capacity, then session is closed
   * with status code [[StatusCode.Registry.MessageTooBig MessageTooBig]].
   */
  def messageCapacity: Int

  /**
   * Sets capacity of incoming message.
   *
   * @param size message capacity in bytes
   *
   * @return this session
   *
   * @note If message exceeds specified capacity, then session is closed
   * with status code [[StatusCode.Registry.MessageTooBig MessageTooBig]].
   */
  def messageCapacity(size: Int): this.type

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
   * Sends text message asynchronously.
   *
   * @param message text message
   */
  def sendAsync[T](message: String): Future[Unit]

  /**
   * Sends binary message.
   *
   * @param message binary message
   */
  def send(message: Array[Byte]): Unit

  /**
   * Sends binary message asynchronously.
   *
   * @param message binary message
   */
  def sendAsync[T](message: Array[Byte]): Future[Unit]

  /**
   * Sends text message.
   *
   * @param message reader to message
   */
  def send(message: Reader): Unit

  /**
   * Sends text message asynchronously.
   *
   * @param message reader to message
   */
  def sendAsync[T](message: Reader): Future[Unit]

  /**
   * Sends binary message.
   *
   * @param message input stream to message
   */
  def send(message: InputStream): Unit

  /**
   * Sends binary message asynchronously.
   *
   * @param message input stream to message
   */
  def sendAsync[T](message: InputStream): Future[Unit]

  /**
   * Sends ping message.
   *
   * @param data application data to accompany ping message
   */
  def ping(data: Array[Byte] = Array.empty): Unit

  /**
   * Sends ping message asynchronously.
   *
   * @param data application data to accompany ping message
   */
  def pingAsync[T](data: Array[Byte] = Array.empty): Future[Unit]

  /**
   * Sends pong message.
   *
   * @param data application data to accompany pong message
   */
  def pong(data: Array[Byte] = Array.empty): Unit

  /**
   * Sends pong message asynchronously.
   *
   * @param data application data to accompany pong message
   */
  def pongAsync[T](data: Array[Byte] = Array.empty): Future[Unit]

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

/** Provides factory for `WebSocketSession`. */
object WebSocketSession:
  /**
   * Wraps WebSocket session around an already established client connection.
   *
   * @param conn WebSocket connection
   * @param id WebSocket identifier
   * @param target target URI for which connection was established
   * @param version WebSocket protocol version
   * @param deflate indicates whether `permessage-deflate` is enabled
   */
  def forClient(conn: WebSocketConnection, id: String, target: Uri, version: String, deflate: Boolean): WebSocketSession =
    WebSocketSessionImpl(id, target, version)(conn, false, if deflate then DeflateMode.Message else DeflateMode.None)

  /**
   * Wraps WebSocket session around an already established client connection.
   *
   * @param socket socket from which WebSocket connection is constructed
   * @param id WebSocket identifier
   * @param target target URI for which connection was established
   * @param version WebSocket protocol version
   * @param deflate indicates whether `permessage-deflate` is enabled
   */
  def forClient(socket: Socket, id: String, target: Uri, version: String, deflate: Boolean): WebSocketSession =
    forClient(WebSocketConnection(socket), id, target, version, deflate)

  /**
   * Wraps WebSocket session around an already established server connection.
   *
   * @param conn WebSocket connection
   * @param id WebSocket identifier
   * @param target target URI for which connection was established
   * @param version WebSocket protocol version
   * @param deflate indicates whether `permessage-deflate` is enabled
   */
  def forServer(conn: WebSocketConnection, id: String, target: Uri, version: String, deflate: Boolean): WebSocketSession =
    WebSocketSessionImpl(id, target, version)(conn, true, if deflate then DeflateMode.Message else DeflateMode.None)

  /**
   * Wraps WebSocket session around an already established server connection.
   *
   * @param socket socket from which WebSocket connection is constructed
   * @param id WebSocket identifier
   * @param target target URI for which connection was established
   * @param version WebSocket protocol version
   * @param deflate indicates whether `permessage-deflate` is enabled
   */
  def forServer(socket: Socket, id: String, target: Uri, version: String, deflate: Boolean): WebSocketSession =
    forServer(WebSocketConnection(socket), id, target, version, deflate)

  private[scamper] def forServer(socket: Socket, id: String, target: Uri, version: String, deflate: DeflateMode): WebSocketSession =
    WebSocketSessionImpl(id, target, version)(WebSocketConnection(socket), true, deflate)
