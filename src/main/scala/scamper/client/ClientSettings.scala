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

/**
 * Configures and creates `HttpClient`.
 *
 * `ClientSettings` is a mutable structure. With each applied change, the
 * settings are modified and returned. After the desired settings are applied,
 * the client is created using one of several factory methods.
 *
 * @constructor Creates default client setttings.
 *
 * === Default Settings ===
 *
 * | Key             | Value |
 * | --------------- | ----- |
 * | bufferSize      | `8192` |
 * | readTimeout     | `30000` |
 * | continueTimeout | `1000` |
 * | incoming        | ''(Not set)'' |
 * | outgoing        | ''(Not set)'' |
 * <br>
 */
class ClientSettings {
  private var settings = DefaultHttpClient.Settings()

  /** Resets to default settings. */
  def reset(): this.type = synchronized {
    settings = DefaultHttpClient.Settings()
    this
  }

  /**
   * Sets buffer size.
   *
   * The buffer size specifies the size in bytes of socket's send/receive
   * buffer.
   */
  def bufferSize(size: Int): this.type = synchronized {
    settings = settings.copy(bufferSize = size)
    this
  }

  /**
   * Sets read timeout.
   *
   * The read timeout controls how long (in milliseconds) a read from socket
   * blocks before it times out, whereafter the client throws `SocketTimeoutException`.
   */
  def readTimeout(timeout: Int): this.type = synchronized {
    settings = settings.copy(readTimeout = timeout)
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
  def continueTimeout(timeout: Int): this.type = synchronized {
    settings = settings.copy(continueTimeout = timeout)
    this
  }

  /** Adds supplied request filter. */
  def outgoing(filter: RequestFilter): this.type = synchronized {
    settings = settings.copy(outgoing = settings.outgoing :+ filter)
    this
  }

  /** Adds supplied response filter. */
  def incoming(filter: ResponseFilter): this.type = synchronized {
    settings = settings.copy(incoming = settings.incoming :+ filter)
    this
  }

  /** Creates client using current settings. */
  def create(): HttpClient = synchronized {
    DefaultHttpClient(settings)
  }

  /**
   * Creates client using current settings and supplied truststore.
   *
   * @param truststore used for SSL/TLS requests ''(store type must be JKS)''
   */
  def create(truststore: File): HttpClient = synchronized {
    DefaultHttpClient(settings, truststore)
  }

  /**
   * Creates client using current settings and supplied trust manager.
   *
   * @param trustManager used for SSL/TLS requests
   */
  def create(trustManager: TrustManager): HttpClient = synchronized {
    DefaultHttpClient(settings, trustManager)
  }
}

/** Provides factory methods for `ClientSettings`. */
object ClientSettings {
  /** Creates default `ClientSettings`. */
  def apply(): ClientSettings = new ClientSettings()
}
