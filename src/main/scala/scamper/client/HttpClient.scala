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
package scamper.client

import scamper.{ Entity, Header, HttpRequest, Uri }
import scamper.Validate.notNull
import scamper.cookies.{ CookieStore, PlainCookie }
import scamper.types.{ ContentCodingRange, MediaRange }
import scamper.websocket.WebSocketSessionHandler

/**
 * Defines HTTP client.
 *
 * A client is created using either the [[HttpClient$.apply HttpClient]] object
 * or [[ClientSettings]].
 */
trait HttpClient {
  /** Gets accepted content types. */
  def accept: Seq[MediaRange]

  /** Gets accepted encodings. */
  def acceptEncoding: Seq[ContentCodingRange]

  /** Gets buffer size. */
  def bufferSize: Int

  /** Gets read timeout. */
  def readTimeout: Int

  /** Gets continue timeout. */
  def continueTimeout: Int

  /** Gets cookie store. */
  def cookies: CookieStore

  /**
   * Sends request and passes response to supplied handler.
   *
   * @param request outgoing request
   * @param handler response handler
   *
   * @return value from response handler
   *
   * @throws java.lang.IllegalArgumentException if `request.target` is not
   * absolute
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
   * @throws java.lang.IllegalArgumentException if `target` is not absolute
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
   * @throws java.lang.IllegalArgumentException if `target` is not absolute
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
   * @throws java.lang.IllegalArgumentException if `target` is not absolute
   */
  def put[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty)
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
   * @throws java.lang.IllegalArgumentException if `target` is not absolute
   */
  def delete[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

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
   * @throws java.lang.IllegalArgumentException if `target` is not WebSocket URI
   * (i.e., it must be absolute URI having scheme of either `"ws"` or `"wss"`)
   *
   * @throws scamper.websocket.WebSocketHandshakeFailure if WebSocket handshake
   * fails
   */
  def websocket[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: WebSocketSessionHandler[T]): T
}

/** Provides factory for `HttpClient`. */
object HttpClient {
  /** Gets new instance of client settings. */
  def settings(): ClientSettings = new ClientSettings()

  /** Creates client using default settings. */
  def apply(): HttpClient = settings().create()

  /**
   * Sends request and passes response to supplied handler.
   *
   * The request is sent using a client created with default settings.
   *
   * @param request HTTP request
   * @param handler response handler
   *
   * @return value from supplied handler
   *
   * @throws java.lang.IllegalArgumentException if `request.target` is not
   * absolute
   */
  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T = {
    notNull(request)
    notNull(handler)

    apply().send(request)(handler)
  }
}
