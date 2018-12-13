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

import RequestMethods._

/** Includes server related items. */
package object server {
  /** Provides utility for handling incoming request. */
  trait RequestHandler {
    /**
     * Handles incoming request.
     *
     * If handler satisfies request, then it returns a response. Otherwise, it
     * returns a request, which may be the original request or an alternate one.
     */
    def apply(request: HttpRequest): Either[HttpRequest, HttpResponse]

    /**
     * Composes this handler with other, using this as a fallback.
     *
     * If `other` returns a request, then the request is passed to `this`.
     * Otherwise, if `other` returns a response, then `this` is not invoked.
     *
     * @param other initial handler
     */
    def compose(other: RequestHandler): RequestHandler =
      req => other(req).left.flatMap(req => apply(req))

    /**
     * Composes this handler with other, using other as a fallback.
     *
     * If `this` returns a request, then the request is passed to `other`.
     * Otherwise, if `this` returns a response, then `other` is not invoked.
     *
     * @param other fallback handler
     */
    def orElse(other: RequestHandler): RequestHandler =
      req => apply(req).left.flatMap(req => other(req))
  }

  /** Provides utility for filtering incoming request. */
  trait RequestFilter extends RequestHandler {
    /**
     * Filters incoming request.
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
     * Processes incoming request.
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
     * Filters outgoing response.
     *
     * The filter may return the original response or an alternate one.
     *
     * @param response outgoing response
     */
    def apply(response: HttpResponse): HttpResponse
  }

  /** Indicates parameter does not exist. */
  case class ParameterNotFound(name: String) extends HttpException(name)

  /** Indicates parameter cannot be converted. */
  case class ParameterNotConvertible(name: String, value: String) extends HttpException(s"$name=$value")

  /** Provides access to server-side parameters associated with request. */
  trait RequestParameters {
    /**
     * Gets named parameter as `String`.
     *
     * @param name parameter name
     *
     * @throws ParameterNotFound if parameter does not exist
     */
    def getString(name: String): String

    /**
     * Gets named parameter as `Int`.
     *
     * @param name parameter name
     *
     * @throws ParameterNotFound if parameter does not exist
     * @throws ParameterNotConvertible if parameter cannot be converted
     */
    def getInt(name: String): Int

    /**
     * Gets named parameter as `Long`.
     *
     * @param name parameter name
     *
     * @throws ParameterNotFound if parameter does not exist
     * @throws ParameterNotConvertible if parameter cannot be converted
     */
    def getLong(name: String): Long
  }

  /**
   * Provides handle to server instance.
   *
   * @see [[HttpServer$ HttpServer]], [[ServerApplication]]
   */
  trait HttpServer {
    /** Gets host address. */
    def host: InetAddress

    /** Gets port number. */
    def port: Int

    /** Gets log file. */
    def log: File

    /** Gets pool size. */
    def poolSize: Int

    /** Gets queue size. */
    def queueSize: Int

    /** Gets read timeout. */
    def readTimeout: Int

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
    /** Gets default server application. */
    def app(): ServerApplication = new ServerApplication()

    /**
     * Creates `HttpServer` at given port using default application and supplied
     * processor.
     *
     * @param port port number
     * @param processor request processor
     *
     * @return server
     */
    def create(port: Int)(processor: RequestProcessor): HttpServer =
      create(InetAddress.getLocalHost(), port)(processor)

    /**
     * Creates `HttpServer` at given host and port using default application and
     * supplied processor.
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
     * Creates `HttpServer` at given host and port using default application and
     * supplied processor.
     *
     * @param host host address
     * @param port port number
     * @param processor request processor
     *
     * @return server
     */
    def create(host: InetAddress, port: Int)(processor: RequestProcessor): HttpServer =
      app().request(processor).create(host, port)
  }

  /**
   * Used to configure and create `HttpServer`.
   *
   * `ServerApplication` is a mutable structure. With each applied change, the
   * application is modified and returned. Changes applied after creating a
   * server are not effected in the server.
   *
   * @constructor Creates default server application.
   *
   * === Default Application ===
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
  class ServerApplication {
    private var app = DefaultHttpServer.Application()

    /** Resets application to default configuration. */
    def reset(): this.type = synchronized {
      app = DefaultHttpServer.Application()
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
     * @return this application
     */
    def queueSize(size: Int): this.type = synchronized {
      app = app.copy(queueSize = size)
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
     * @return this application
     */
    def poolSize(size: Int): this.type = synchronized {
      app = app.copy(poolSize = size)
      this
    }

    /**
     * Sets read timeout.
     *
     * The `readTimeout` controls how long a read from a socket blocks before it
     * times out. At which point, the socket is closed, and its associated
     * request is discarded.
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
     * Sets location of server log file.
     *
     * @param file log file
     *
     * @return this application
     */
    def log(file: File): this.type = synchronized {
      app = app.copy(log = file.getCanonicalFile())
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
     * <strong>Note:</strong> The password can be discarded after invoking this
     * method.
     *
     * @param keyStore server key store
     * @param password key store password
     * @param storeType key store type (i.e., JKS, JCEKS, etc.)
     *
     * @return this application
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
     * Adds request handler to serve static files from given directory.
     *
     * The directory files are mapped based on the request target path.
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
     * @return this application
     */
    def static(baseDirectory: File): this.type = synchronized {
      app = app.copy(requestHandlers = app.requestHandlers :+ StaticFileServer(baseDirectory, "/"))
      this
    }

    /**
     * Adds request handler to serve static files from given directory.
     *
     * The directory files are mapped based on the request target path minus
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
     * @return this application
     */
    def static(path: String, baseDirectory: File): this.type = synchronized {
      app = app.copy(requestHandlers = app.requestHandlers :+ StaticFileServer(baseDirectory, path))
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
      DefaultHttpServer(host, port, app)
    }
  }

  /** Provides factory for `ServerApplication`. */
  object ServerApplication {
    /** Creates default `ServerApplication`. */
    def apply(): ServerApplication = new ServerApplication()
  }
}
