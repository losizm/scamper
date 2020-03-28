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

import scamper.{ HttpMessage, HttpRequest, HttpResponse }

/** Provides utility for handling incoming request. */
trait RequestHandler {
  /**
   * Handles incoming request.
   *
   * If handler satisfies the request, then it returns an HttpResponse.
   * Otherwise, it returns an HttpRequest, which can be either the
   * original request or an alternate one.
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
  def coalesce(handlers: Seq[RequestHandler]): RequestHandler = {
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

  /**
   * Composes head handler with tail handlers, using tail handlers as
   * fallbacks.
   *
   * @param one request handler
   * @param two another request handler
   * @param more additional request handlers
   */
  def coalesce(one: RequestHandler, two: RequestHandler, more: RequestHandler*): RequestHandler =
    coalesce(one +: two +: more)
}
