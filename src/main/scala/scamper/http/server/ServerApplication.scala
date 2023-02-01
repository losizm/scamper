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
package server

import java.io.File
import java.net.InetAddress

import scamper.http.types.KeepAliveParameters

import Validate.{ noNulls, notNull }

/**
 * Defines server application for creating `HttpServer`.
 *
 * ### Default Configuration
 *
 * The initial application is constructed with the following default
 * configuration:
 *
 * | Key         | Value |
 * | ---------   | ----- |
 * | backlogSize | `50` |
 * | poolSize    | `Runtime.getRuntime().availableProcessors()` |
 * | queueSize   | `Runtime.getRuntime().availableProcessors() * 4` |
 * | bufferSize  | `8192` |
 * | readTimeout | `5000` |
 * | headerLimit | `100` |
 * | keepAlive   | _(Not configured)_ |
 * | secure      | _(Not configured)_ |
 * | trigger     | _(Not configured)_ |
 * | incoming    | _(Not configured)_ |
 * | outgoing    | _(Not configured)_ |
 * | recover     | _(Sends `500 Internal Server Error`)_ |
 *
 * ### Building HTTP Server
 *
 * `ServerApplication` is a mutable structure. With each applied change, the
 * application is modified and returned. After the desired configuration is
 * applied, a server is created using a factory method.
 *
 * {{{
 * import java.io.File
 *
 * import scala.language.implicitConversions
 *
 * import scamper.http.{ BodyParser, stringToEntity }
 * import scamper.http.ResponseStatus.Registry.{ NotFound, NoContent, Ok }
 * import scamper.http.server.{ *, given }
 *
 * // Get server application
 * val app = ServerApplication()
 *
 * // Add request handler to log all requests
 * app.incoming { req =>
 *   println(req.startLine)
 *   req
 * }
 *
 * // Add request handler for GET requests at specified path
 * app.get("/about") { req =>
 *   Ok("This server is powered by Scamper.")
 * }
 *
 * // Add request handler for PUT requests at specified path
 * app.put("/data/:id") { req =>
 *   def update(id: Int, data: String): Boolean = ???
 *
 *   given BodyParser[String] = BodyParser.string()
 *
 *   // Get path parameter
 *   val id = req.params.getInt("id")
 *
 *   update(id, req.as[String]) match
 *     case true  => NoContent()
 *     case false => NotFound()
 * }
 *
 * // Serve files from file directory
 * app.files("/main", File("/path/to/public"))
 *
 * // Gzip response body if not empty
 * app.outgoing { res =>
 *   res.body.isKnownEmpty match
 *     case true  => res
 *     case false => res.setGzipContentEncoding()
 * }
 *
 * // Create server
 * val server = app.toHttpServer(8080)
 *
 * try
 *   printf("Host: %s%n", server.host)
 *   printf("Port: %d%n", server.port)
 *
 *   // Run server for 60 seconds
 *   Thread.sleep(60 * 1000)
 * finally
 *   // Close server when done
 *   server.close()
 * }}}
 */
class ServerApplication extends Router:
  private var app = HttpServerImpl.Application()

  /**
   * @inheritdoc
   *
   * @return `"/"`
   */
  def mountPath: String =
    "/"

  /** @inheritdoc */
  def reset(): this.type = synchronized {
    app = HttpServerImpl.Application()
    this
  }

  /**
   * Sets logger name.
   *
   * @param name logger name
   */
  def logger(name: String): this.type = synchronized {
    app = app.copy(loggerName = Option(name))
    this
  }

  /**
   * Sets backlog size.
   *
   * The `backlogSize` specifies the maximum number of incoming connections that
   * can wait before being accepted. Incoming connections that exceed this limit
   * are refused.
   *
   * @param size backlog size
   *
   * @return this application
   */
  def backlogSize(size: Int): this.type = synchronized {
    app = app.copy(backlogSize = size)
    this
  }

  /**
   * Sets pool size.
   *
   * The `poolSize` specifies the maximum number of requests processed
   * concurrently.
   *
   * @param size pool size
   *
   * @return this application
   */
  def poolSize(size: Int): this.type = synchronized {
    app = app.copy(poolSize = size)
    this
  }

  /**
   * Sets queue size.
   *
   * The `queueSize` specifies the maximum number of requests that can be queued
   * for processing. Incoming requests that exceed this limit are sent
   * 503 (Service Unavailable).
   *
   * @param size queue size
   *
   * @return this application
   */
  def queueSize(size: Int): this.type = synchronized {
    app = app.copy(queueSize = size)
    this
  }

  /**
   * Sets buffer size.
   *
   * The `bufferSize` specifies in bytes the size of buffer used when reading
   * from and writing to socket.
   *
   * The `bufferSize` also determines the maximum length of any header line.
   * Incoming requests containing a header that exceeds this limit are sent
   * 431 (Request Header Fields Too Large).
   *
   * @param size buffer size in bytes
   *
   * @return this application
   *
   * @note `bufferSize` is also used as the optimal chunk size when writing a
   *   response with chunked transfer encoding.
   */
  def bufferSize(size: Int): this.type = synchronized {
    app = app.copy(bufferSize = size)
    this
  }

  /**
   * Sets read timeout.
   *
   * The `readTimeout` specifies how long a read from a socket blocks before it
   * times out, whereafter 408 (Request Timeout) is sent to client.
   *
   * @param timeout read timeout in milliseconds
   *
   * @return this application
   */
  def readTimeout(timeout: Int): this.type = synchronized {
    app = app.copy(readTimeout = timeout)
    this
  }

  /**
   * Sets header limit.
   *
   * The `headerLimit` specifies the maximum number of headers allowed. Incoming
   * requests containing headers that exceed this limit are sent
   * 431 (Request Header Fields Too Large).
   *
   * @param limit header limit
   *
   * @return this application
   */
  def headerLimit(limit: Int): this.type = synchronized {
    app = app.copy(headerLimit = limit)
    this
  }

  /**
   * Enables persistent connections using specified parameters.
   *
   * @param params keep-alive parameters
   *
   * @return this application
   */
  def keepAlive(params: KeepAliveParameters): this.type = synchronized {
    app = app.copy(keepAlive = Option(params))
    this
  }

  /**
   * Enables persistent connections using specified timeout and max.
   *
   * @param timeout idle timeout in seconds
   * @param max maximum number of requests per connection
   *
   * @return this application
   */
  def keepAlive(timeout: Int, max: Int): this.type = synchronized {
    app = app.copy(keepAlive = Some(KeepAliveParameters(timeout, max)))
    this
  }

  /**
   * Sets key store to be used for SSL/TLS.
   *
   * @param keyStore server key store
   * @param password key store password
   * @param storeType key store type (i.e., JKS, JCEKS, etc.)
   *
   * @return this application
   */
  def secure(keyStore: File, password: String, storeType: String): this.type = synchronized {
    app = app.copy(serverSocketFactory = SecureServerSocketFactory.create(keyStore, password.toCharArray, storeType))
    this
  }

  /**
   * Sets key store to be used for SSL/TLS.
   *
   * @param keyStore server key store
   * @param password key store password
   * @param storeType key store type (i.e., JKS, JCEKS, etc.)
   *
   * @return this application
   *
   * @note The password can be discarded after invoking this method.
   */
  def secure(keyStore: File, password: Array[Char], storeType: String): this.type = synchronized {
    app = app.copy(serverSocketFactory = SecureServerSocketFactory.create(keyStore, password, storeType))
    this
  }

  /**
   * Sets key and certificate to be used for SSL/TLS.
   *
   * @param key private key
   * @param certificate public key certificate
   *
   * @return this application
   */
  def secure(key: File, certificate: File): this.type = synchronized {
    app = app.copy(serverSocketFactory = SecureServerSocketFactory.create(key, certificate))
    this
  }

  /** @inheritdoc */
  def trigger(hook: LifecycleHook): this.type = synchronized {
    app = app.copy(lifecycleHooks = app.lifecycleHooks :+ notNull(hook))
    this
  }

  /** @inheritdoc */
  def incoming(handler: RequestHandler): this.type = synchronized {
    app = app.copy(
      requestHandlers = app.requestHandlers :+ handler,
      lifecycleHooks  = addLifecycleHook(handler)
    )
    this
  }

  /** @inheritdoc */
  def incoming(path: String, methods: RequestMethod*)(handler: RequestHandler): this.type =
    app = app.copy(
      requestHandlers = app.requestHandlers :+ TargetRequestHandler(path, methods, handler),
      lifecycleHooks  = addLifecycleHook(handler)
    )
    this

  /** @inheritdoc */
  def outgoing(filter: ResponseFilter): this.type = synchronized {
    app = app.copy(
      responseFilters = app.responseFilters :+ notNull(filter, "filter"),
      lifecycleHooks  = addLifecycleHook(filter)
    )
    this
  }

  /** @inheritdoc */
  def recover(handler: ErrorHandler): this.type = synchronized {
    app = app.copy(
      errorHandlers  = app.errorHandlers :+ notNull(handler, "handler"),
      lifecycleHooks = addLifecycleHook(handler)
    )
    this
  }

  /**
   * Creates server at given port.
   *
   * @param port port number
   *
   * @return new server
   */
  def toHttpServer(port: Int): HttpServer =
    toHttpServer("0.0.0.0", port)

  /**
   * Creates server at given host and port.
   *
   * @param host host address
   * @param port port number
   *
   * @return new server
   */
  def toHttpServer(host: String, port: Int): HttpServer =
    toHttpServer(InetAddress.getByName(host), port)

  /**
   * Creates server at given host and port.
   *
   * @param host host address
   * @param port port number
   *
   * @return new server
   */
  def toHttpServer(host: InetAddress, port: Int): HttpServer = synchronized {
    HttpServerImpl(host, port, app)
  }

  private def addLifecycleHook[T](value: T): Seq[LifecycleHook] =
    value match
      case hook: LifecycleHook => app.lifecycleHooks :+ hook
      case _                   => app.lifecycleHooks
