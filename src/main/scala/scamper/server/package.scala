/*
 * Copyright 2018 Carlos Conyers
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

import java.io.File
import java.net.InetAddress

import javax.net.ServerSocketFactory

/** Includes server related items. */
package object server {
  /** Provides utility for handling HTTP request. */
  trait RequestHandler {
    /**
     * Handles request.
     *
     * If handler satisfies request, then it returns a response. Otherwise, it
     * returns a request, which can be original request or alternate one.
     */
    def apply(request: HttpRequest): Either[HttpRequest, HttpResponse]
  }

  /** Provides utility for filtering HTTP request. */
  trait RequestFilter extends RequestHandler {
    /**
     * Filters request.
     *
     * The filter can return the original request or an alternate one.
     *
     * @param request incoming request
     */
    def filter(request: HttpRequest): HttpRequest

    def apply(request: HttpRequest): Either[HttpRequest, HttpResponse] =
      Left(filter(request))
  }

  /** Provides utility for processing HTTP request. */
  trait RequestProcessor extends RequestHandler {
    /**
     * Processes request.
     *
     * The processor returns a response that satisfies the request.
     *
     * @param request incoming request
     */
    def process(request: HttpRequest): HttpResponse

    def apply(request: HttpRequest): Either[HttpRequest, HttpResponse] =
      Right(process(request))
  }

  /**
   * HTTP Server
   *
   * @see [[HttpServer$ HttpServer]], [[ServerConfiguration]]
   */
  trait HttpServer {
    /** Gets host address. */
    def host: InetAddress

    /** Gets port number. */
    def port: Int

    /** Tests whether server is HTTPS. */
    def isSecure: Boolean

    /** Closes server. */
    def close(): Unit

    /**
     * Tests whether server is closed.
     *
     * @return `true` if server is closed; `false` otherwise
     */
    def isClosed: Boolean
  }

  /** Provides factory for configuring and creating `HttpServer`. */
  object HttpServer {
    /** Gets default server configuration. */
    def configure(): ServerConfiguration = new ServerConfiguration()

    /**
     * Creates `HttpServer` at given port with default configuration and
     * supplied processor.
     *
     * @param port port number
     * @param processor request processor
     *
     * @return server
     */
    def create(port: Int)(processor: RequestProcessor): HttpServer =
      create(InetAddress.getLocalHost(), port)(processor)

    /**
     * Creates `HttpServer` at given host and port with default configuration
     * and supplied processor.
     *
     * @param host host address
     * @param port port number
     * @param processor request processor
     *
     * @return server
     */
    def create(host: String, port: Int)(processor: RequestProcessor): HttpServer =
      create(InetAddress.getByName(host), port)(processor)

    /**
     * Creates `HttpServer` at given host and port with default configuration
     * and supplied processor.
     *
     * @param host host address
     * @param port port number
     * @param processor request processor
     *
     * @return server
     */
    def create(host: InetAddress, port: Int)(processor: RequestProcessor): HttpServer =
      configure().include(processor).create(host, port)
  }

  /**
   * Used to configure and create `HttpServer`.
   *
   * `ServerConfiguration` is a mutable, thread-safe structure. With each
   * requested change, the configuration is modified and returned. Changes
   * applied after creating server are not effected in server.
   *
   * @constructor Creates default server configuration.
   *
   * === Default Configuration ===
   *
   * |Key          |Value |
   * |-------------|------|
   * |poolSize     |`Runtime.getRuntime().availableProcessors()`|
   * |queueSize    |`Runtime.getRuntime().availableProcessors() * 4`|
   * |readTimeout  |`5000`|
   * |log          |`new File("server.log")`|
   * |secure       |<em>(non-secure)</em>|
   * |include      |<em>(no handlers)</em>|
   */
  class ServerConfiguration {
    private var config = BlockingHttpServer.Configuration()

    /** Resets configuration to default values. */
    def reset(): this.type = synchronized {
      config = BlockingHttpServer.Configuration()
      this
    }

    /**
     * Sets queue size.
     *
     * @param size queue size
     *
     * @return this configuration
     */
    def queueSize(size: Int): this.type = synchronized {
      config = config.copy(queueSize = size)
      this
    }

    /**
     * Sets pool size.
     *
     * @param size pool size
     *
     * @return this configuration
     */
    def poolSize(size: Int): this.type = synchronized {
      config = config.copy(poolSize = size)
      this
    }

    /**
     * Sets read timeout.
     *
     * @param timeout read timeout in milliseconds
     *
     * @return this configuration
     */
    def readTimeout(timeout: Int): this.type = synchronized {
      config = config.copy(readTimeout = timeout)
      this
    }

    /**
     * Sets log to given file.
     *
     * @param file log file
     *
     * @return this configuration
     */
    def log(file: File): this.type = synchronized {
      config = config.copy(log = file)
      this
    }

    /**
     * Sets protocol to `http`.
     *
     * @return this configuration
     */
    def nonSecure(): this.type = synchronized {
      config = config.copy(factory = ServerSocketFactory.getDefault())
      this
    }

    /**
     * Sets protocol to `https`.
     *
     * When server is created it will be configured to use supplied key store.
     *
     * @param keyStore server key store
     * @param password key store password
     * @param storeType key store type (i.e., JKS, JCEKS, etc.)
     *
     * @return this configuration
     */
    def secure(keyStore: File, password: String, storeType: String): this.type = synchronized {
      config = config.copy(factory = SecureServerSocketFactory.create(keyStore, password.toCharArray, storeType))
      this
    }

    /**
     * Sets protocol to `https`.
     *
     * When server is created it will be configured to use supplied key store.
     *
     * <strong>Note:</strong> The password can be discarded after invoking this
     * method.
     *
     * @param keyStore server key store
     * @param password key store password
     * @param storeType key store type (i.e., JKS, JCEKS, etc.)
     *
     * @return this configuration
     */
    def secure(keyStore: File, password: Array[Char], storeType: String): this.type = synchronized {
      config = config.copy(factory = SecureServerSocketFactory.create(keyStore, password, storeType))
      this
    }

    /**
     * Sets protocol to `https`.
     *
     * When server is created it will be configured to use supplied key and
     * certificate.
     *
     * @param key private key
     * @param certificate public key certificate
     *
     * @return this configuration
     */
    def secure(key: File, certificate: File): this.type = synchronized {
      config = config.copy(factory = SecureServerSocketFactory.create(key, certificate))
      this
    }

    /**
     * Includes supplied request handler.
     *
     * The handler is appended to existing request handler chain.
     *
     * @return this configuration
     */
    def include(handler: RequestHandler): this.type = synchronized {
      config = config.copy(handlers = config.handlers :+ handler)
      this
    }

    /**
     * Includes supplied request filter.
     *
     * The filter is appended to existing request handler chain.
     *
     * @return this configuration
     */
    def include(filter: RequestFilter): this.type = synchronized {
      config = config.copy(handlers = config.handlers :+ filter)
      this
    }

    /**
     * Includes supplied request processor.
     *
     * The processor is appended to existing request handler chain.
     *
     * @return this configuration
     */
    def include(processor: RequestProcessor): this.type = synchronized {
      config = config.copy(handlers = config.handlers :+ processor)
      this
    }

    /**
     * Creates `HttpServer` at given port.
     *
     * @param port port number
     *
     * @return new server
     */
    def create(port: Int): HttpServer = synchronized {
      create(InetAddress.getLocalHost(), port)
    }

    /**
     * Creates `HttpServer` at given host and port.
     *
     * @param host host address
     * @param port port number
     *
     * @return new server
     */
    def create(host: String, port: Int): HttpServer = synchronized {
      create(InetAddress.getByName(host), port)
    }

    /**
     * Creates `HttpServer` at given host and port.
     *
     * @param host host address
     * @param port port number
     *
     * @return new server
     */
    def create(host: InetAddress, port: Int): HttpServer = synchronized {
      BlockingHttpServer(host, port, config)
    }
  }

  /** Provides factory for `ServerConfiguration`. */
  object ServerConfiguration {
    /** Creates default `ServerConfiguration`. */
    def apply(): ServerConfiguration = new ServerConfiguration()
  }
}
