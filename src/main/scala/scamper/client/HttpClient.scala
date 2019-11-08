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

import java.io.File

import javax.net.ssl.TrustManager

import scala.util.Try

import scamper._
import scamper.Auxiliary.UriType
import scamper.RequestMethod.Registry._
import scamper.cookies.{ PlainCookie, RequestCookies }

/**
 * Provides utility for sending request and handling response.
 *
 * A client is created from either [[HttpClient.apply HttpClient.apply()]] or
 * [[HttpClient.Settings]].
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
}

/** Provides factory methods for creating `HttpClient`. */
object HttpClient {
  /**
   * Configures and creates `HttpClient`.
   *
   * `Settings` is a mutable structure. With each applied change, the
   * settings are modified and returned. After the desired settings are applied,
   * the client is created using one of several factory methods.
   *
   * === Default Settings ===
   *
   * | Key             | Value |
   * | --------------- | ----- |
   * | bufferSize      | `8192` |
   * | readTimeout     | `30000` |
   * | continueTimeout | `1000` |
   * | truststore      | ''(Not set)'' |
   * | trustManager    | ''(Not set)'' |
   * <br>
   */
  class Settings private[HttpClient]() {
    private var _bufferSize: Int = 8192
    private var _readTimeout: Int = 30000
    private var _continueTimeout: Int = 1000

    /**
     * Sets buffer size.
     *
     * The buffer size specifies the size in bytes of the socket's send/receive
     * buffer.
     */
    def bufferSize(size: Int): this.type = {
      _bufferSize = size
      this
    }

    /**
     * Sets read timeout.
     *
     * The read timeout controls how long (in milliseconds) a read from a socket
     * blocks before it times out, whereafter the client throws `SocketTimeoutException`.
     */
    def readTimeout(timeout: Int): this.type = {
      _readTimeout = timeout
      this
    }

    /**
     * Sets continue timeout.
     *
     * The continue timeout specifies how long to wait (in milliseconds) for a
     * '''100 Continue''' response before sending the request body.
     *
     * @note This applies only to requests that include `Except: 100-Continue`
     *   header and request body.
     */
    def continueTimeout(timeout: Int): this.type = {
      _continueTimeout = timeout
      this
    }

    /** Creates client using current settings. */
    def create(): HttpClient =
      DefaultHttpClient(_bufferSize, _readTimeout, _continueTimeout)

    /**
     * Creates client using current settings and supplied truststore.
     *
     * @param truststore used for SSL/TLS requests ''(store type must be JKS)''
     */
    def create(truststore: File): HttpClient =
      DefaultHttpClient(_bufferSize, _readTimeout, _continueTimeout, truststore)

    /**
     * Creates client using current settings and supplied trust manager.
     *
     * @param trustManager used for SSL/TLS requests
     */
    def create(trustManager: TrustManager): HttpClient =
      DefaultHttpClient(_bufferSize, _readTimeout, _continueTimeout, trustManager)
  }

  /**
   * Creates `HttpClient` with supplied settings.
   *
   * @param bufferSize socket buffer size
   * @param readTimeout socket read timeout
   * @param continueTimeout how long to wait (in milliseconds) for '''100 Continue''' before sending request body
   */
  def apply(bufferSize: Int = 8192, readTimeout: Int = 30000, continueTimeout: Int = 1000): HttpClient =
    DefaultHttpClient(bufferSize, readTimeout, continueTimeout)

  /** Gets default client settings. */
  def settings(): Settings = new Settings()

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
  def send[T](request: HttpRequest, bufferSize: Int = 8192, readTimeout: Int = 30000, continueTimeout: Int = 1000)(handler: ResponseHandler[T]): T =
    HttpClient(bufferSize, readTimeout, continueTimeout).send(request)(handler)
}
