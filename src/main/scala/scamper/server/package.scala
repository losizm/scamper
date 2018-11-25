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

import java.net.InetAddress

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

  /** Indicates no response was generated for given request. */
  case class RequestNotSatisfied(request: HttpRequest) extends HttpException

  /** Provides utility for applying chain of request handlers to request. */
  object RequestHandlerChain {
    /**
     * Sends request through chain of request handlers in search of response.
     *
     * The chain is broken upon first generated response. That is, the handlers
     * are invoked up to first generated response, and the remaining handlers are
     * bypassed.
     *
     * @throws RequestNotSatisfied if no response is generated
     */
    def getResponse(request: HttpRequest, handlers: Seq[RequestHandler]): HttpResponse = {
      val init: Either[HttpRequest, HttpResponse] = Left(request)

      handlers.foldLeft(init) { (prev, handler) =>
        prev.fold(req => handler(req), res => Right(res))
      } match {
        case Right(response) => response
        case Left(request) => throw RequestNotSatisfied(request)
      }
    }
  }

  /** HTTP Server */
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
}
