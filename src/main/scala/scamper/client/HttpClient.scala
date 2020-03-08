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
   * @note To make effective use of this method, `request.target` must be an
   *   absolute URI.
   */
  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T

  /**
   * Sends GET request and passes response to handler.
   *
   * @param target request target
   * @param header request headers
   * @param cookies request cookies
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @note To make effective use of this method, `target` must be an absolute URI.
   */
  def get[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

  /**
   * Sends POST request and passes response to handler.
   *
   * @param target request target
   * @param header request headers
   * @param cookies request cookies
   * @param body message body
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @note To make effective use of this method, `target` must be an absolute URI.
   */
  def post[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T

  /**
   * Sends PUT request and passes response to handler.
   *
   * @param target request target
   * @param header request headers
   * @param cookies request cookies
   * @param body message body
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @note To make effective use of this method, `target` must be an absolute URI.
   */
  def put[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T

  /**
   * Sends PATCH request and passes response to handler.
   *
   * @param target request target
   * @param header request headers
   * @param cookies request cookies
   * @param body message body
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @note To make effective use of this method, `target` must be an absolute URI.
   */
  def patch[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T

  /**
   * Sends DELETE request and passes response to handler.
   *
   * @param target request target
   * @param header request headers
   * @param cookies request cookies
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @note To make effective use of this method, `target` must be an absolute URI.
   */
  def delete[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

  /**
   * Sends HEAD request and passes response to handler.
   *
   * @param target request target
   * @param header request headers
   * @param cookies request cookies
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @note To make effective use of this method, `target` must be an absolute URI.
   */
  def head[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

  /**
   * Sends OPTIONS request and passes response to handler.
   *
   * @param target request target
   * @param header request headers
   * @param cookies request cookies
   * @param body message body
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @note To make effective use of this method, `target` must be an absolute URI.
   */
  def options[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
    (handler: ResponseHandler[T]): T

  /**
   * Sends TRACE request and passes response to handler.
   *
   * @param target request target
   * @param header request headers
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @note To make effective use of this method, `target` must be an absolute URI.
   */
  def trace[T](target: Uri, headers: Seq[Header] = Nil)(handler: ResponseHandler[T]): T

  /**
   * Connects to WebSocket server at given target and passes established session
   * to supplied handler.
   *
   * @param target WebSocket target
   * @param header additional headers to include in WebSocket request
   * @param handler WebSocket session handler
   *
   * @return value from session handler
   *
   * @note To make effective use of this method, `target` must be a WebSocket
   *  URI. That is, it must be an absolute URI having a scheme of either `"ws"`
   *  or `"wss"` (secure).
   */
  def websocket[T](target: Uri, headers: Seq[Header] = Nil)(handler: WebSocketSession => T): T
}

/** Provides factory methods for creating `HttpClient`. */
object HttpClient {
  /** Gets default client settings. */
  def settings(): ClientSettings = new ClientSettings()

  /**
   * Creates `HttpClient` with supplied settings.
   *
   * @param bufferSize socket buffer size
   * @param readTimeout socket read timeout
   * @param continueTimeout how long to wait (in milliseconds) for '''100 Continue''' before sending request body
   */
  def apply(bufferSize: Int = 8192, readTimeout: Int = 30000, continueTimeout: Int = 1000): HttpClient =
    settings().bufferSize(bufferSize).readTimeout(readTimeout).continueTimeout(continueTimeout).create()

  /**
   * Sends request and passes response to supplied handler.
   *
   * @param request HTTP request
   * @param bufferSize socket buffer size
   * @param readTimeout socket read timeout
   * @param continueTimeout how long to wait (in milliseconds) for '''100 Continue''' before sending request body
   * @param handler response handler
   *
   * @return value from applied handler
   *
   * @note To make effective use of this method, `request.target` must be an
   *  absolute URI.
   *
   * @note The `continueTimeout` applies only if request includes `Except: 100-Continue`
   *  header and request body.
   */
  def send[T](request: HttpRequest, bufferSize: Int = 8192, readTimeout: Int = 30000, continueTimeout: Int = 1000)(handler: ResponseHandler[T]): T = {
    HttpClient(bufferSize, readTimeout, continueTimeout).send(request)(notNull(handler))
  }
}
