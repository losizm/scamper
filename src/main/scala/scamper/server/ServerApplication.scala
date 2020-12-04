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
package scamper.server

import java.io.File
import java.net.InetAddress

import scamper.RequestMethod
import scamper.Validate._
import scamper.logging.{ Logger, LogWriter }
import scamper.types.KeepAliveParameters
import scamper.websocket.WebSocketSession
import RequestMethod.Registry._

/**
 * Configures and creates `HttpServer`.
 *
 * `ServerApplication` is a mutable structure. With each applied change, the
 * application is modified and returned. After the desired settings are applied,
 * a server is created using one of several factory methods.
 *
 * @constructor Creates server application.
 *
 * === Default Configuration ===
 *
 * | Key         | Value |
 * | ---------   | ----- |
 * | logger      | `scamper.logging.ConsoleLogger` |
 * | backlogSize | `50` |
 * | poolSize    | `Runtime.getRuntime().availableProcessors()` |
 * | queueSize   | `Runtime.getRuntime().availableProcessors() * 4` |
 * | bufferSize  | `8192` |
 * | readTimeout | `5000` |
 * | headerLimit | `100` |
 * | keepAlive   | ''(Not configured)'' |
 * | secure      | ''(Not configured)'' |
 * | incoming    | ''(Not configured)'' |
 * | outgoing    | ''(Not configured)'' |
 * | error       | ''(Sends `500 Internal Server Error`)'' |
 * <br>
 */
class ServerApplication {
  private var app = HttpServerImpl.Application()

  /** Resets application to default configuration. */
  def reset(): this.type = synchronized {
    app = HttpServerImpl.Application()
    this
  }

  /**
   * Sets logger to given file.
   *
   * @param file file to which server logs are written
   *
   * @return this application
   *
   * @note If file exists, it is opened in append mode.
   */
  def logger(file: File): this.type = synchronized {
    app = app.copy(logger = LogWriter(notNull(file), true))
    this
  }

  /**
   * Sets logger.
   *
   * @param logger logger to which server logs are written
   *
   * @return this application
   */
  def logger(logger: Logger): this.type = synchronized {
    app = app.copy(logger = logger)
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
   * '''503 Service Unavailable'''.
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
   * '''431 Request Header Fields Too Large'''.
   *
   * @param size buffer size (in bytes)
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
   * times out, whereafter '''408 Request Timeout''' is sent to client.
   *
   * @param timeout read timeout (in milliseconds)
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
   * '''431 Request Header Fields Too Large'''.
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
   * Enables keep-alive connections using specified parameters.
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
   * Enables keep-alive connections using specified timeout and max.
   *
   * @param timeout connection timeout (in seconds)
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

  /**
   * Adds supplied request handler.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param handler request handler
   *
   * @return this application
   */
  def incoming(handler: RequestHandler): this.type =
    incoming("*") { handler }

  /**
   * Adds supplied handler for requests with given path and any of supplied
   * request methods.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path request path
   * @param methods request methods
   * @param handler request handler
   *
   * @return this application
   *
   * @note If no request methods are specified, then matches are limited to path
   * only.
   */
  def incoming(path: String, methods: RequestMethod*)(handler: RequestHandler): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(notNull(handler), notNull(path), noNulls(methods)))
    this
  }

  /**
   * Adds supplied handler for GET requests to given path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path request path
   * @param handler request handler
   *
   * @return this application
   */
  def get(path: String)(handler: RequestHandler): this.type =
    incoming(path, Get) { handler }

  /**
   * Adds supplied handler for POST requests to given path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path request path
   * @param handler request handler
   *
   * @return this application
   */
  def post(path: String)(handler: RequestHandler): this.type =
    incoming(path, Post) { handler }

  /**
   * Adds supplied handler for PUT requests to given path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path request path
   * @param handler request handler
   *
   * @return this application
   */
  def put(path: String)(handler: RequestHandler): this.type =
    incoming(path, Put) { handler }

  /**
   * Adds supplied handler for DELETE requests to given path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path request path
   * @param handler request handler
   *
   * @return this application
   */
  def delete(path: String)(handler: RequestHandler): this.type =
    incoming(path, Delete) { handler }

  /**
   * Adds request handler at mount path to serve files from given source directory.
   *
   * The mount path is stripped from the request path, and the remaining path is
   * used to locate files within the source directory.
   *
   * === File Mapping Examples ===
   *
   * | Mount Path | Source Directory | Request Path              | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | /tmp             | /images/logo.png          | /tmp/logo.png |
   * | /images    | /tmp             | /images/icons/warning.png | /tmp/icons/warning.png |
   *
   * @param path request path at which directory is mounted
   * @param source base directory from which files are served
   *
   * @return this application
   */
  def files(path: String, source: File): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ StaticFileServer(path, source))
    this
  }

  /**
   * Adds request handler at mount path to serve resources from given source
   * directory.
   *
   * The mount path is stripped from the request path, and the remaining path is
   * used to locate resources within the source directory
   *
   * === Resource Mapping Examples ===
   *
   * | Mount Path | Source Directory | Request Path              | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | assets           | /images/logo.png          | assets/logo.png |
   * | /images    | assets           | /images/icons/warning.png | assets/icons/warning.png |
   *
   * @param path request path at which directory is mounted
   * @param source base directory from which resources are served
   *
   * @return this application
   *
   * @note The current thread's context class loader is used to load resources.
   */
  def resources(path: String, source: String): this.type =
    resources(path, source, Thread.currentThread.getContextClassLoader)

  /**
   * Adds request handler at mount path to serve resources from given source
   * directory.
   *
   * The mount path is stripped from the request path, and the remaining path is
   * used to locate resources within the source directory
   *
   * === Resource Mapping Examples ===
   *
   * | Mount Path | Source Directory | Request Path              | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | assets           | /images/logo.png          | assets/logo.png |
   * | /images    | assets           | /images/icons/warning.png | assets/icons/warning.png |
   *
   * @param path request path at which directory is mounted
   * @param source base directory from which resources are served
   * @param loader class loader with which resources are loaded
   *
   * @return this application
   */
  def resources(path: String, source: String, loader: ClassLoader): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ StaticResourceServer(path, source, loader))
    this
  }

  /**
   * Adds WebSocket server at given path using supplied session handler for each
   * connection.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path WebSocket path
   * @param handler WebSocket session handler
   *
   * @return this application
   */
  def websocket[T](path: String)(handler: WebSocketSession => T): this.type =
    incoming(path, Get) { WebSocketRequestHandler(notNull(handler)) }

  /**
   * Adds routing application at given mount path.
   *
   * A router is created and passed to routing application, and the routing
   * application adds request handlers to router.
   *
   * @param path request path at which routing application is mounted
   * @param routing routing application
   *
   * @return this application
   */
  def use[T](path: String)(routing: Router => T): this.type = synchronized {
    routing(new RouterImpl(this, path))
    this
  }

  /**
   * Adds supplied response filter.
   *
   * The filter is appended to existing response filter chain.
   *
   * @param filter response filter
   *
   * @return this application
   */
  def outgoing(filter: ResponseFilter): this.type = synchronized {
    app = app.copy(responseFilters = app.responseFilters :+ notNull(filter))
    this
  }

  /**
   * Sets error handler.
   *
   * @param handler error handler
   *
   * @return this application
   */
  def error(handler: ErrorHandler): this.type = {
    app = app.copy(errorHandler = Option(handler))
    this
  }

  /**
   * Creates server at given port.
   *
   * @param port port number
   *
   * @return new server
   */
  def create(port: Int): HttpServer = synchronized {
    create("0.0.0.0", port)
  }

  /**
   * Creates server at given host and port.
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
   * Creates server at given host and port.
   *
   * @param host host address
   * @param port port number
   *
   * @return new server
   */
  def create(host: InetAddress, port: Int): HttpServer = synchronized {
    HttpServerImpl(host, port, app)
  }
}

/** Provides factory for `ServerApplication`. */
object ServerApplication {
  /** Gets new instance of `ServerApplication`. */
  def apply(): ServerApplication = new ServerApplication()
}
