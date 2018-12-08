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
  /** Provides utility for handling incoming request. */
  trait RequestHandler {
    /**
     * Handles request.
     *
     * If handler satisfies request, then it returns a response. Otherwise, it
     * returns a request, which may be the original request or an alternate one.
     */
    def apply(request: HttpRequest): Either[HttpRequest, HttpResponse]
  }

  /** Provides utility for filtering incoming request. */
  trait RequestFilter extends RequestHandler {
    /**
     * Filters request.
     *
     * The filter may return the original request or an alternate one.
     *
     * @param request incoming request
     */
    def filter(request: HttpRequest): HttpRequest

    def apply(request: HttpRequest): Either[HttpRequest, HttpResponse] =
      Left(filter(request))
  }

  /** Provides utility for processing incoming request. */
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

  /** Provides utility for filtering outgoing response. */
  trait ResponseFilter {
    /**
     * Filters response.
     *
     * The filter may return the original response or an alternate one.
     *
     * @param response outgoing response prior to filtering
     */
    def apply(response: HttpResponse): HttpResponse
  }

  /** Provides access to server-side parameters associated with request. */
  trait RequestParameters {
    /**
     * Gets named parameter as `String`.
     *
     * @param name parameter name
     *
     * @throws NoSuchElementException if parameter not present
     */
    def getString(name: String): String

    /**
     * Gets named parameter as `Int`.
     *
     * @param name parameter name
     *
     * @throws NoSuchElementException if parameter not present
     * @throws NumberFormatException if parameter cannot be converted to `Int`
     */
    def getInt(name: String): Int

    /**
     * Gets named parameter as `Long`.
     *
     * @param name parameter name
     *
     * @throws NoSuchElementException if parameter not present
     * @throws NumberFormatException if parameter cannot be converted to `Long`
     */
    def getLong(name: String): Long
  }

  /**
   * Provides handle to server instance.
   *
   * @see [[HttpServer$ HttpServer]], [[ServerConfiguration]]
   */
  trait HttpServer {
    /** Gets host address. */
    def host: InetAddress

    /** Gets port number. */
    def port: Int

    /** Tests whether server is secure. */
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
    def config(): ServerConfiguration = new ServerConfiguration()

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
      config().request(processor).create(host, port)
  }

  /**
   * Used to configure and create `HttpServer`.
   *
   * `ServerConfiguration` is a mutable structure. With each applied change, the
   * configuration is modified and returned. Changes applied after creating a
   * server are not effected in the server.
   *
   * @constructor Creates default server configuration.
   *
   * === Default Configuration ===
   *
   * |Key        |Value |
   * |-----------|------|
   * |poolSize   |`Runtime.getRuntime().availableProcessors()`|
   * |queueSize  |`Runtime.getRuntime().availableProcessors() * 4`|
   * |readTimeout|`5000`|
   * |log        |`new File("server.log")`|
   * |secure     |<em>(Not configured)</em>|
   * |request    |<em>(Not configured)</em>|
   * |response   |<em>(Not configured)</em>|
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
     * The `queueSize` sets the number of requests that are permitted to wait
     * for processing. Incoming requests that would exceed this limit are
     * discarded.
     *
     * <strong>Note:</strong> `queueSize` is also used to configure server
     * backlog (i.e., backlog of incoming connections), so technically there can
     * be up to double `queueSize` waiting to be processed if both request queue
     * and server backlog are filled.
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
     * The `poolSize` specifies the maximum number of requests that are
     * processed concurrently.
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
     * The `readTimeout` controls how long a read from a socket will block
     * before it times out. At which point, the socket is closed, and its
     * associated request is discarded.
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
     * Sets location of server log file.
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
     * Sets key store to be used for SSL/TLS.
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
     * Sets key store to be used for SSL/TLS.
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
     * Sets key and certificate to be used for SSL/TLS.
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
     * @param handler request handler
     *
     * @return this configuration
     */
    def request(handler: RequestHandler): this.type = synchronized {
      config = config.copy(requestHandlers = config.requestHandlers :+ handler)
      this
    }

    /**
     * Includes supplied request filter.
     *
     * The filter is appended to existing request handler chain.
     *
     * @param filter request filter
     *
     * @return this configuration
     */
    def request(filter: RequestFilter): this.type = synchronized {
      config = config.copy(requestHandlers = config.requestHandlers :+ filter)
      this
    }

    /**
     * Includes supplied request processor.
     *
     * The processor is appended to existing request handler chain.
     *
     * @param processor request processor
     *
     * @return this configuration
     */
    def request(processor: RequestProcessor): this.type = synchronized {
      config = config.copy(requestHandlers = config.requestHandlers :+ processor)
      this
    }

    /**
     * Includes supplied processor for requests with given path.
     *
     * The processor is appended to existing request handler chain.
     *
     * @param path request path
     * @param processor request processor
     *
     * @return this configuration
     */
    def request(path: String)(processor: RequestProcessor): this.type = synchronized {
      config = config.copy(requestHandlers = config.requestHandlers :+ TargetedRequestHandler(processor, path, None))
      this
    }

    /**
     * Includes supplied processor for requests with given method and path.
     *
     * The processor is appended to existing request handler chain.
     *
     * @param method request method
     * @param path request path
     * @param processor request processor
     *
     * @return this configuration
     */
    def request(method: RequestMethod, path: String)(processor: RequestProcessor): this.type = synchronized {
      config = config.copy(requestHandlers = config.requestHandlers :+ TargetedRequestHandler(processor, path, Some(method)))
      this
    }

    /**
     * Includes request handler that serves static files from given directory.
     *
     * The directory files are mapped based on the request's target path.
     *
     * === Example Mappings ===
     *
     * |Base Directory|Request Target Path      |Maps to    |
     * |--------------|-------------------------|-----------|
     * |/tmp          |/images/logo.png         |/tmp/images/logo.png|
     * |/tmp          |/images/icons/warning.png|/tmp/images/icons/warning.png|
     * |/tmp          |/styles/main.css         |/tmp/styles/main.css|
     *
     * @param baseDirectory base directory from which files are served
     *
     * @return this configuration
     */
    def request(baseDirectory: File): this.type = synchronized {
      config = config.copy(requestHandlers = config.requestHandlers :+ StaticFileServer(baseDirectory, "/"))
      this
    }

    /**
     * Includes request handler that serves static files from given directory.
     *
     * The directory files are mapped based on the request's target path minus
     * prefix.
     *
     * === Example Mappings ===
     *
     * |Path   |Base Directory|Request Target Path      |Maps to    |
     * |-------|--------------|-------------------------|-----------|
     * |/images|/tmp          |/images/logo.png         |/tmp/logo.png|
     * |/images|/tmp          |/images/icons/warning.png|/tmp/icons/warning.png|
     * |/images|/tmp          |/styles/main.css         |<em>Doesn't map to anything</em>|
     *
     * @param path request equest
     * @param baseDirectory base directory from which files are served
     *
     * @return this configuration
     */
    def request(path: String, baseDirectory: File): this.type = synchronized {
      config = config.copy(requestHandlers = config.requestHandlers :+ StaticFileServer(baseDirectory, path))
      this
    }

    /**
     * Includes supplied response filter.
     *
     * The filter is appended to existing response filter chain.
     *
     * @param filter response filter
     *
     * @return this configuration
     */
    def response(filter: ResponseFilter): this.type = synchronized {
      config = config.copy(responseFilters = config.responseFilters :+ filter)
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
