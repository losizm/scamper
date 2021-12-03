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
package client

import scamper.http.cookies.{ CookieStore, PlainCookie }
import scamper.http.types.{ ContentCodingRange, MediaRange }
import scamper.http.websocket.WebSocketApplication

import Validate.notNull

/**
 * Defines HTTP client.
 *
 * A client can be created using the [[HttpClient$.apply HttpClient]] companion
 * object.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.http.{ BodyParser, stringToUri }
 * import scamper.http.client.HttpClient
 *
 * given BodyParser[String] = BodyParser.string()
 *
 * // Create HttpClient instance
 * val client = HttpClient()
 *
 * def getMessageOfTheDay(): Either[Int, String] =
 *   // Use client instance
 *   client.get("http://localhost:8080/motd") { res =>
 *     res.isSuccessful match
 *       case true  => Right(res.as[String])
 *       case false => Left(res.statusCode)
 *   }
 * }}}
 *
 * And, if a given client is in scope, you can make use of `send()` on the
 * request itself.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.http.{ BodyParser, stringToUri }
 * import scamper.http.RequestMethod.Registry.Get
 * import scamper.http.client.{ ClientHttpRequest, HttpClient }
 * import scamper.http.headers.{ Accept, AcceptLanguage }
 * import scamper.http.types.{ stringToMediaRange, stringToLanguageRange }
 *
 * given HttpClient = HttpClient()
 * given BodyParser[String] = BodyParser.string(4096)
 *
 * Get("http://localhost:8080/motd")
 *   .setAccept("text/plain")
 *   .setAcceptLanguage("en-US; q=0.6", "fr-CA; q=0.4")
 *   .send(res => println(res.as[String])) // Send request using client
 * }}}
 *
 * See also [[ClientSettings]] for information about configuring the HTTP
 * client before it is created.
 */
trait HttpClient:
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
   * Connects to WebSocket server at target path and passes established session
   * to supplied application.
   *
   * @param target WebSocket target
   * @param headers additional headers to include in WebSocket request
   * @param cookies cookies to include in WebSocket request
   * @param application WebSocket application
   *
   * @return value from application
   *
   * @throws java.lang.IllegalArgumentException if `target` is not WebSocket URI
   * (i.e., it must be absolute URI with either "ws" or "wss" scheme)
   *
   * @throws scamper.http.websocket.WebSocketHandshakeFailure if WebSocket
   * handshake fails
   */
  def websocket[T](target: Uri, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(application: WebSocketApplication[T]): T

/** Provides factory for `HttpClient`. */
object HttpClient:
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
  def send[T](request: HttpRequest)(handler: ResponseHandler[T]): T =
    notNull(request, "request")
    notNull(handler, "handler")

    apply().send(request)(handler)
