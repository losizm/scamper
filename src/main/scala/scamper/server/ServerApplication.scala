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
package scamper.server

import java.io.File
import java.net.InetAddress

import scamper.RequestMethod
import scamper.RequestMethods._
import scamper.logging.{ Logger, LogWriter }

/**
 * Configures and creates `HttpServer`.
 *
 * `ServerApplication` is a mutable structure. With each applied change, the
 * application is modified and returned. Changes applied after creating a
 * server are not effected in the server.
 *
 * @constructor Creates default server application.
 *
 * === Default Application ===
 *
 * | Key         | Value |
 * | ---------   | ----- |
 * | poolSize    | `Runtime.getRuntime().availableProcessors()` |
 * | queueSize   | `Runtime.getRuntime().availableProcessors() * 4` |
 * | bufferSize  | `8192` |
 * | readTimeout | `5000` |
 * | logger      | `ConsoleLogger` |
 * | secure      | ''(Not configured)'' |
 * | error       | ''(Default error handler &ndash; sends `500 Internal Server Error`)'' |
 * | request     | ''(Not configured)'' |
 * | response    | ''(Not configured)'' |
 * <br>
 */
class ServerApplication {
  private var app = DefaultHttpServer.Application()

  /** Resets application to default configuration. */
  def reset(): this.type = synchronized {
    app = DefaultHttpServer.Application()
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
   * The `queueSize` specifies the maximum number of requests permitted to
   * wait for processing. Incoming requests that would exceed this limit are
   * discarded.
   *
   * @param size queue size
   *
   * @return this application
   *
   * @note `queueSize` is also used to configure server backlog (i.e., backlog
   *   of incoming connections), so technically there can be up to double
   *   `queueSize` waiting to be processed if both request queue and server
   *   backlog are filled.
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
   * The `readTimeout` controls how long a read from a socket blocks before it
   * times out, whereafter '''408 Request Timeout''' is sent to client.
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
   * Sets logger to given file.
   *
   * @param file file to which server logs are written
   *
   * @return this application
   *
   * @note If file exists, it is opened in append mode.
   */
  def logger(file: File): this.type = synchronized {
    app = app.copy(logger = LogWriter(file, true))
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
   * Sets key store to be used for SSL/TLS.
   *
   * @param keyStore server key store
   * @param password key store password
   * @param storeType key store type (i.e., JKS, JCEKS, etc.)
   *
   * @return this application
   */
  def secure(keyStore: File, password: String, storeType: String): this.type = synchronized {
    app = app.copy(factory = SecureServerSocketFactory.create(keyStore, password.toCharArray, storeType))
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
    app = app.copy(factory = SecureServerSocketFactory.create(keyStore, password, storeType))
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
    app = app.copy(factory = SecureServerSocketFactory.create(key, certificate))
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
   * Adds supplied request handler.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param handler request handler
   *
   * @return this application
   */
  def request(handler: RequestHandler): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ handler)
    this
  }

  /**
   * Adds supplied request filter.
   *
   * The filter is appended to existing request handler chain.
   *
   * @param filter request filter
   *
   * @return this application
   */
  def request(filter: RequestFilter): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ filter)
    this
  }

  /**
   * Adds supplied request processor.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param processor request processor
   *
   * @return this application
   */
  def request(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ processor)
    this
  }

  /**
   * Adds supplied processor for requests with given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def request(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, None))
    this
  }

  /**
   * Adds supplied processor for requests with given method and path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param method request method
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def request(method: RequestMethod, path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(method)))
    this
  }

  /**
   * Adds supplied processor for GET requests to given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def get(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(GET)))
    this
  }

  /**
   * Adds supplied processor for POST requests to given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def post(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(POST)))
    this
  }

  /**
   * Adds supplied processor for PUT requests to given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def put(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(PUT)))
    this
  }

  /**
   * Adds supplied processor for PATCH requests to given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def patch(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(PATCH)))
    this
  }

  /**
   * Adds supplied processor for DELETE requests to given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def delete(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(DELETE)))
    this
  }

  /**
   * Adds supplied processor for HEAD requests to given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def head(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(HEAD)))
    this
  }

  /**
   * Adds supplied processor for OPTIONS requests to given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def options(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(OPTIONS)))
    this
  }

  /**
   * Adds supplied processor for TRACE requests to given path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path request path
   * @param processor request processor
   *
   * @return this application
   */
  def trace(path: String)(processor: RequestProcessor): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ TargetedRequestHandler(processor, path, Some(TRACE)))
    this
  }

  /**
   * Adds request handler at mount point to serve files from given source directory.
   *
   * The mount point is stripped from the request path, and the resulting path is
   * used to locate files within source directory.
   *
   * === File Mapping Examples ===
   *
   * | Mount Point | Source Directory | Request Path              | Maps to |
   * | ----------- | ---------------- | ------------------------- | ------- |
   * | /images     | /tmp             | /images/logo.png          | /tmp/logo.png |
   * | /images     | /tmp             | /images/icons/warning.png | /tmp/icons/warning.png |
   * | /images     | /tmp             | /styles/main.css          | ''Doesn't map to anything'' |
   *
   * @param mountPoint request path at which directory is mounted
   * @param sourceDirectory source directory from which files are served
   *
   * @return this application
   */
  def files(mountPoint: String, sourceDirectory: File): this.type = synchronized {
    app = app.copy(requestHandlers = app.requestHandlers :+ StaticFileServer(mountPoint, sourceDirectory))
    this
  }

  /**
   * Adds request handler at mount point to serve resources from given base name.
   *
   * The mount point is stripped from the request path, and the resulting path is
   * used to locate resources starting at base name.
   *
   * === Resource Mapping Examples ===
   *
   * | Mount Point | Base Name | Request Path              | Maps to |
   * | ----------- | --------- | ------------------------- | ------- |
   * | /images     | assets    | /images/logo.png          | assets/logo.png |
   * | /images     | assets    | /images/icons/warning.png | assets/icons/warning.png |
   * | /images     | assets    | /styles/main.css          | ''Doesn't map to anything'' |
   *
   * @param mountPoint request path at which resources are mounted
   * @param baseName base name from which resources are served
   * @param loader class loader from which resources are loaded
   *
   * @return this application
   *
   * @note If `loader` is not supplied, then the current thread's context class
   *   loader is used.
   */
  def resources(mountPoint: String, baseName: String, loader: Option[ClassLoader] = None): this.type = synchronized {
    val effectiveLoader = loader.getOrElse(Thread.currentThread.getContextClassLoader)
    app = app.copy(requestHandlers = app.requestHandlers :+ StaticResourceServer(mountPoint, baseName, effectiveLoader))
    this
  }

  /**
   * Adds routing application at given mount point.
   *
   * A router is created and passed to routing application, and the routing
   * application adds request handlers to router.
   *
   * @param mountPoint request path at which routing application is mounted
   * @param routing routing application
   *
   * @return this application
   */
  def use[T](mountPoint: String)(routing: Router => T): this.type = synchronized {
    routing(new DefaultRouter(this, mountPoint))
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
  def response(filter: ResponseFilter): this.type = synchronized {
    app = app.copy(responseFilters = app.responseFilters :+ filter)
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
    create("0.0.0.0", port)
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
    DefaultHttpServer(app, host, port)
  }
}

/** Provides factory for `ServerApplication`. */
object ServerApplication {
  /** Creates default `ServerApplication`. */
  def apply(): ServerApplication = new ServerApplication()
}

