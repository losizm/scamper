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

import scamper.Validate.notNull

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
 * | trust           | ''(Not set)'' |
 * | incoming        | ''(Not set)'' |
 * | outgoing        | ''(Not set)'' |
 * <br>
 */
class ClientSettings {
  private var settings = HttpClientImpl.Settings()

  /** Resets to default settings. */
  def reset(): this.type = synchronized {
    settings = HttpClientImpl.Settings()
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

  /**
   * Sets truststore.
   *
   * @param truststore truststore used for HTTPS connections
   * @param storeType store type (e.g., JKS or PKCS12)
   * @param password store password
   */
  def trust(truststore: File, storeType: String = "JKS", password: Option[String] = None): this.type = synchronized {
    settings = settings.copy(secureSocketFactory = SecureSocketFactory.create(truststore, storeType, password))
    this
  }

  /**
   * Sets trust manager.
   *
   * @param manager trust manager used for HTTPS connections
   */
  def trust(manager: TrustManager): this.type = synchronized {
    settings = settings.copy(secureSocketFactory = SecureSocketFactory.create(manager))
    this
  }

  /** Adds supplied request filter. */
  def outgoing(filter: RequestFilter): this.type = synchronized {
    settings = settings.copy(outgoing = settings.outgoing :+ notNull(filter))
    this
  }

  /** Adds supplied response filter. */
  def incoming(filter: ResponseFilter): this.type = synchronized {
    settings = settings.copy(incoming = settings.incoming :+ notNull(filter))
    this
  }

  /** Creates client using current settings. */
  def create(): HttpClient = synchronized {
    HttpClientImpl(settings)
  }
}

/** Provides factory methods for `ClientSettings`. */
object ClientSettings {
  /** Creates default `ClientSettings`. */
  def apply(): ClientSettings = new ClientSettings()
}
