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
package scamper

import java.io.File
import java.net.InetAddress

import RequestMethods._
import logging.Logger

/**
 * Provides HTTP server implementation.
 *
 * === Building HTTP Server ===
 *
 * To build a server, you begin with `ServerApplication`. This is a mutable
 * structure to which you apply changes to configure the server. Once the desired
 * settings are applied, you invoke one of several methods to create the server.
 *
 * {{{
 * import java.io.File
 * import scamper.BodyParsers
 * import scamper.Implicits.stringToEntity
 * import scamper.ResponseStatuses.{ NotFound, Ok }
 * import scamper.server.HttpServer
 * import scamper.server.Implicits._
 *
 * // Get server application
 * val app = HttpServer.app()
 *
 * // Add request handler to log all requests
 * app.incoming { req =>
 *   println(req.startLine)
 *   req
 * }
 *
 * // Add request handler to specific request method and path
 * app.get("/about") { req => Ok("This server is powered by Scamper.") }
 *
 * // Add request handler using path parameter
 * app.put("/data/:id") { req =>
 *   def update(id: Int, data: String): Boolean = ???
 *
 *   implicit val parser = BodyParsers.text()
 *
 *   // Get path parameter
 *   val id = req.params.getInt("id")
 *
 *   update(id, req.as[String]) match {
 *     case true  => Ok()
 *     case false => NotFound()
 *   }
 * }
 *
 * // Serve static files
 * app.files("/main", new File("/path/to/public"))
 *
 * // Gzip response body if not empty
 * app.outgoing { res =>
 *   res.body.isKnownEmpty match {
 *     case true  => res
 *     case false => res.withGzipContentEncoding()
 *   }
 * }
 *
 * // Create server
 * val server = app.create(8080)
 *
 * printf("Host: %s%n", server.host)
 * printf("Port: %d%n", server.port)
 *
 * Thread.sleep(60 * 1000)
 *
 * // Close server when done
 * server.close()
 * }}}
 */
package object server {
  /** Provides utility for handling incoming request. */
  trait RequestHandler {
    /**
     * Handles incoming request.
     *
     * If handler can satisfy the request, then it should return an
     * HttpResponse.  Otherwise, it should return an HttpRequest, which can be
     * either the original request or an alternate one.
     */
    def apply(request: HttpRequest): HttpMessage

    /**
     * Composes this handler with other, using this as a fallback.
     *
     * If `other` returns a request, then the request is passed to `this`.
     * Otherwise, if `other` returns a response, then `this` is not invoked.
     *
     * @param other initial handler
     */
    def compose(other: RequestHandler): RequestHandler =
      other(_) match {
        case req: HttpRequest  => apply(req)
        case res: HttpResponse => res
      }

    /**
     * Composes this handler with other, using other as a fallback.
     *
     * If `this` returns a request, then the request is passed to `other`.
     * Otherwise, if `this` returns a response, then `other` is not invoked.
     *
     * @param other fallback handler
     */
    def orElse(other: RequestHandler): RequestHandler =
      apply(_) match {
        case req: HttpRequest  => other(req)
        case res: HttpResponse => res
      }
  }

  /** Provides `RequestHandler` utilities. */
  object RequestHandler {
    /**
     * Composes head handler with tail handlers, using tail handlers as
     * fallbacks.
     *
     * @param handlers request handlers
     *
     * @note If `handlers` is empty, a request handler is created that returns
     *   the request it receives.
     */
    def coalesce(handlers: RequestHandler*): RequestHandler = {
      @annotation.tailrec
      def handle(req: HttpRequest, handlers: Seq[RequestHandler]): HttpMessage =
        handlers match {
          case Nil          => req
          case head +: tail =>
            head(req) match {
              case req: HttpRequest  => handle(req, tail)
              case res: HttpResponse => res
            }
        }
      handle(_, handlers)
    }
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

    /**
     * Composes this filter and other, with other applied first.
     *
     * @param other other filter
     */
    def compose(other: ResponseFilter): ResponseFilter =
      req => apply(other(req))

    /**
     * Composes this filter and other, with this applied first.
     *
     * @param other other filter
     */
    def andThen(other: ResponseFilter): ResponseFilter =
      req => other(apply(req))
  }

  /** Provides `ResponseFilter` utilities. */
  object ResponseFilter {
    /**
     * Composes chain of response filters, with response of preceding filter
     * passed to its successor.
     *
     * @param filters response filters
     *
     * @note If `filters` is empty, a response filter is created that returns
     *   the response it receives.
     */
    def chain(filters: ResponseFilter*): ResponseFilter = {
      @annotation.tailrec
      def filter(res: HttpResponse, filters: Seq[ResponseFilter]): HttpResponse =
        filters match {
          case Nil          => res
          case head +: tail => filter(head(res), tail)
        }
      filter(_, filters)
    }
  }

  /** Provides utility for handling error when servicing request. */
  trait ErrorHandler {
    /**
     * Creates response for given error.
     *
     * @param error error generated when servicing request
     * @param request request for which error was generated
     */
    def apply(error: Throwable, request: HttpRequest): HttpResponse
  }

  /**
   * Indicates response was aborted.
   *
   * A `RequestHandler` throws `ResponseAborted` if no response should be sent
   * for the handled request.
   */
  case class ResponseAborted(message: String) extends HttpException(message)

  /** Indicates parameter is not found. */
  case class ParameterNotFound(name: String) extends HttpException(name)

  /** Indicates parameter cannot be converted. */
  case class ParameterNotConvertible(name: String, value: String) extends HttpException(s"$name=$value")

  /** Provides access to server-side request parameters. */
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

    /** Gets logger. */
    def logger: Logger

    /** Gets pool size. */
    def poolSize: Int

    /** Gets queue size. */
    def queueSize: Int

    /** Gets buffer size. */
    def bufferSize: Int

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

  /** Provides factory methods for creating `HttpServer`. */
  object HttpServer {
    /** Gets default server application. */
    def app(): ServerApplication = new ServerApplication()

    /**
     * Creates `HttpServer` at given port using supplied handler.
     *
     * @param port port number
     * @param handler request handler
     *
     * @return server
     */
    def create(port: Int)(handler: RequestHandler): HttpServer =
      create("0.0.0.0", port)(handler)

    /**
     * Creates `HttpServer` at given host and port using supplied handler.
     *
     * @param host host address
     * @param port port number
     * @param handler request handler
     *
     * @return server
     */
    def create(host: String, port: Int)(handler: RequestHandler): HttpServer =
      create(InetAddress.getByName(host), port)(handler)

    /**
     * Creates `HttpServer` at given host and port using supplied handler.
     *
     * @param host host address
     * @param port port number
     * @param handler request handler
     *
     * @return server
     */
    def create(host: InetAddress, port: Int)(handler: RequestHandler): HttpServer =
      app().incoming(handler).create(host, port)

    /**
     * Creates `HttpServer` at given port using supplied handler.
     *
     * The server is secured with key and certificate.
     *
     * @param port port number
     * @param key private key
     * @param certificate public key certificate
     * @param handler request handler
     *
     * @return server
     */
    def create(port: Int, key: File, certificate: File)(handler: RequestHandler): HttpServer =
      create("0.0.0.0", port, key, certificate)(handler)

    /**
     * Creates `HttpServer` at given host and port using supplied handler.
     *
     * The server is secured with key and certificate.
     *
     * @param host host address
     * @param port port number
     * @param key private key
     * @param certificate public key certificate
     * @param handler request handler
     *
     * @return server
     */
    def create(host: String, port: Int, key: File, certificate: File)(handler: RequestHandler): HttpServer =
      create(InetAddress.getByName(host), port, key, certificate)(handler)

    /**
     * Creates `HttpServer` at given host and port using supplied handler.
     *
     * The server is secured with key and certificate.
     *
     * @param host host address
     * @param port port number
     * @param key private key
     * @param certificate public key certificate
     * @param handler request handler
     *
     * @return server
     */
    def create(host: InetAddress, port: Int, key: File, certificate: File)(handler: RequestHandler): HttpServer =
      app().incoming(handler).secure(key, certificate).create(host, port)

    /**
     * Creates `HttpServer` at given port using supplied handler.
     *
     * The server is secured with keystore.
     *
     * @param port port number
     * @param keyStore server key store
     * @param password key store password
     * @param storeType key store type (i.e., JKS, JCEKS, etc.)
     * @param handler request handler
     *
     * @return server
     */
    def create(port: Int, keyStore: File, password: String, storeType: String)(handler: RequestHandler): HttpServer =
      create("0.0.0.0", port, keyStore, password, storeType)(handler)

    /**
     * Creates `HttpServer` at given host and port using supplied handler.
     *
     * The server is secured with keystore.
     *
     * @param host host address
     * @param port port number
     * @param keyStore server key store
     * @param password key store password
     * @param storeType key store type (i.e., JKS, JCEKS, etc.)
     * @param handler request handler
     *
     * @return server
     */
    def create(host: String, port: Int, keyStore: File, password: String, storeType: String)(handler: RequestHandler): HttpServer =
      create(InetAddress.getByName(host), port, keyStore, password, storeType)(handler)

    /**
     * Creates `HttpServer` at given host and port using supplied handler.
     *
     * The server is secured with keystore.
     *
     * @param host host address
     * @param port port number
     * @param keyStore server key store
     * @param password key store password
     * @param storeType key store type (i.e., JKS, JCEKS, etc.)
     * @param handler request handler
     *
     * @return server
     */
    def create(host: InetAddress, port: Int, keyStore: File, password: String, storeType: String)(handler: RequestHandler): HttpServer =
      app().incoming(handler).secure(keyStore, password, storeType).create(host, port)
  }
}
