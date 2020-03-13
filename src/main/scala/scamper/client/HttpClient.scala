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

import scamper.{ Entity, Header, HttpRequest, Uri }
import scamper.Validate.notNull
import scamper.cookies.PlainCookie
import scamper.websocket.WebSocketSession

/**
 * Provides utility for sending request and handling response.
 *
 * A client is created from either [[HttpClient.apply HttpClient.apply()]] or
 * [[ClientSettings]].
 */
trait HttpClient {
  /** Gets buffer size. */
  def bufferSize: Int

  /** Gets read timeout. */
  def readTimeout: Int

  /** Gets continue timeout.  */
  def continueTimeout: Int

  /**
   * Sends request and passes response to supplied handler.
   *
   * @param request outgoing request
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `request.target` is not absolute
   */
  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T

  /**
   * Sends GET request and passes response to handler.
   *
   * @param target request target
   * @param headers request headers
   * @param cookies request cookies
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `target` is not absolute
   */
  def get[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

  /**
   * Sends POST request and passes response to handler.
   *
   * @param target request target
   * @param headers request headers
   * @param cookies request cookies
   * @param body message body
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `target` is not absolute
   */
  def post[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T

  /**
   * Sends PUT request and passes response to handler.
   *
   * @param target request target
   * @param headers request headers
   * @param cookies request cookies
   * @param body message body
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `target` is not absolute
   */
  def put[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T

  /**
   * Sends PATCH request and passes response to handler.
   *
   * @param target request target
   * @param headers request headers
   * @param cookies request cookies
   * @param body message body
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `target` is not absolute
   */
  def patch[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T

  /**
   * Sends DELETE request and passes response to handler.
   *
   * @param target request target
   * @param headers request headers
   * @param cookies request cookies
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `target` is not absolute
   */
  def delete[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

  /**
   * Sends HEAD request and passes response to handler.
   *
   * @param target request target
   * @param headers request headers
   * @param cookies request cookies
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `target` is not absolute
   */
  def head[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

  /**
   * Sends OPTIONS request and passes response to handler.
   *
   * @param target request target
   * @param headers request headers
   * @param cookies request cookies
   * @param body message body
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `target` is not absolute
   */
  def options[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T

  /**
   * Sends TRACE request and passes response to handler.
   *
   * @param target request target
   * @param headers request headers
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws IllegalArgumentException if `target` is not absolute
   */
  def trace[T](target: Uri, headers: Seq[Header] = Nil)(handler: ResponseHandler[T]): T

  /**
   * Connects to WebSocket server at given target and passes established session
   * to supplied handler.
   *
   * @param target WebSocket target
   * @param headers additional headers to include in WebSocket request
   * @param cookies cookies to include in WebSocket request
   * @param handler WebSocket session handler
   *
   * @return value from session handler
   *
   * @throws IllegalArgumentException if `target` is not WebSocket URI (i.e.,
   *  it must be an absolute URI having a scheme of either `"ws"` or `"wss"`)
   *
   * @throws WebSocketHandshakeFailure if WebSocket handshake fails
   */
  def websocket[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: WebSocketSession => T): T
}

/** Provides factory methods for creating `HttpClient`. */
object HttpClient {
  /** Gets default client settings. */
  def settings(): ClientSettings = new ClientSettings()

  /** Creates `HttpClient` using default settings. */
  def apply(): HttpClient = settings().create()

  /**
   * Sends request and passes response to supplied handler.
   *
   * The request is sent using an `HttpClient` created with default settings.
   *
   * @param request HTTP request
   * @param handler response handler
   *
   * @return value from applied handler
   *
   * @throws IllegalArgumentException if `request.target` is not absolute
   */
  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T = {
    notNull(request)
    notNull(handler)

    apply().send(request)(handler)
  }
}
