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

import java.io.File

import javax.net.ssl.TrustManager

import scamper.http.cookies.CookieStore
import scamper.http.types.{ ContentCodingRange, MediaRange }

/**
 * Defines HTTP client settings.
 *
 * `ClientSettings` is a mutable structure. With each applied change, the
 * settings are modified and returned. After applying the desired settings, a
 * client is created using a factory method.
 *
 * ### Default Settings
 *
 * | Key             | Value |
 * | --------------- | ----- |
 * | accept          | `*`/`*` |
 * | acceptEncodings | `Nil` |
 * | bufferSize      | `8192` |
 * | readTimeout     | `30000` |
 * | continueTimeout | `1000` |
 * | keepAlive       | `false` |
 * | coookies        | `CookieStore.Null` |
 * | resolveTo       | _(Not set)_ |
 * | trust           | _(Not set)_ |
 * | incoming        | _(Not set)_ |
 * | outgoing        | _(Not set)_ |
 *
 * @constructor Creates client settings.
 */
class ClientSettings:
  private var settings = HttpClientImpl.Settings()

  /** Resets to default settings. */
  def reset(): this.type =
    settings = HttpClientImpl.Settings()
    this

  /**
   * Sets authority to which relative targets are resolved.
   *
   * @param secure use https and wss schemes
   */
  def resolveTo(authority: String, secure: Boolean): this.type =
    val baseUri = UriBuilder()
      .scheme(if secure then "https" else "http")
      .authority(authority)
      .toUri

    settings = settings.copy(resolveTo = Some(baseUri))
    this

  /**
   * Sets host and port to which relative targets are resolved.
   *
   * @param secure use https and wss schemes
   */
  def resolveTo(host: String, port: Int, secure: Boolean): this.type =
    val baseUri = UriBuilder()
      .scheme(if secure then "https" else "http")
      .authority(host, port)
      .toUri

    settings = settings.copy(resolveTo = Some(baseUri))
    this

  /**
   * Sets host and port to which relative targets are resolved.
   *
   * @param secure use https and wss schemes
   */
  def resolveTo(host: String, port: Option[Int], secure: Boolean): this.type =
    val baseUri = UriBuilder()
      .scheme(if secure then "https" else "http")
      .authority(host, port)
      .toUri

    settings = settings.copy(resolveTo = Some(baseUri))
    this

  /**
   * Sets accepted content types.
   *
   * The Accept header for each outgoing request is set accordingly.
   */
  def accept(ranges: Seq[MediaRange]): this.type =
    settings = settings.copy(accept = noNulls(ranges, "ranges"))
    this

  /**
   * Sets accepted content types.
   *
   * The Accept header for each outgoing request is set accordingly.
   */
  def accept(one: MediaRange, more: MediaRange*): this.type =
    accept(one +: more)

  /**
   * Sets accepted content encodings.
   *
   * The Accept-Encoding header for each outgoing request is set accordingly.
   */
  def acceptEncoding(ranges: Seq[ContentCodingRange]): this.type =
    settings = settings.copy(acceptEncoding = noNulls(ranges, "ranges"))
    this

  /**
   * Sets accepted content encodings.
   *
   * The Accept-Encoding header for each outgoing request is set accordingly.
   */
  def acceptEncoding(one: ContentCodingRange, more: ContentCodingRange*): this.type =
    acceptEncoding(one +: more)

  /**
   * Sets buffer size.
   *
   * The buffer size specifies the size in bytes of client socket's send and
   * receive buffers.
   */
  def bufferSize(size: Int): this.type =
    settings = settings.copy(bufferSize = size)
    this

  /**
   * Sets read timeout.
   *
   * The read timeout specifies how many milliseconds a read from client socket
   * blocks before it times out, whereafter `SocketTimeoutException` is thrown.
   */
  def readTimeout(timeout: Int): this.type =
    settings = settings.copy(readTimeout = timeout)
    this

  /**
   * Sets continue timeout.
   *
   * The continue timeout specifies how many milliseconds to wait for a 100
   * (Continue) response before sending the request body.
   *
   * @note This applies only to requests that include an Except header set to
   * 100-Continue.
   */
  def continueTimeout(timeout: Int): this.type =
    settings = settings.copy(continueTimeout = timeout)
    this

  /** Enables or disables persistent connections. */
  def keepAlive(enable: Boolean = true): this.type =
    settings = settings.copy(keepAlive = enable)
    this

  /**
   * Sets cookie store.
   *
   * @param cookies cookie store
   */
  def cookies(cookieStore: CookieStore = CookieStore()): this.type =
    settings = settings.copy(cookies = notNull(cookieStore, "cookieStore"))
    this

  /**
   * Sets truststore.
   *
   * @param truststore truststore used for HTTPS connections
   * @param storeType store type (e.g., JKS or PKCS12)
   * @param password store password
   */
  def trust(truststore: File, storeType: String = "JKS", password: Option[String] = None): this.type =
    settings = settings.copy(secureSocketFactory = SecureSocketFactory.create(truststore, storeType, password))
    this

  /**
   * Sets trust manager.
   *
   * @param manager trust manager used for HTTPS connections
   */
  def trust(manager: TrustManager): this.type =
    settings = settings.copy(secureSocketFactory = SecureSocketFactory.create(manager))
    this

  /** Adds supplied request filter. */
  def outgoing(filter: RequestFilter): this.type =
    settings = settings.copy(outgoing = settings.outgoing :+ notNull(filter, "filter"))
    this

  /** Adds supplied response filter. */
  def incoming(filter: ResponseFilter): this.type =
    settings = settings.copy(incoming = settings.incoming :+ notNull(filter, "filter"))
    this

  /** Creates client using current settings. */
  def toHttpClient(): HttpClient = HttpClientImpl(settings)
