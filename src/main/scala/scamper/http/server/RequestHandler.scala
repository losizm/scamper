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

/** Defines handler for incoming request. */
@FunctionalInterface
trait RequestHandler:
  /**
   * Handles incoming request.
   *
   * If handler satisfies request, then it returns a response; otherwise, it
   * returns a request.
   *
   * @param request incoming request
   */
  def apply(request: HttpRequest): HttpMessage

  /**
   * Creates composite handler by applying `this` before `other`.
   *
   * If `this` returns a request, then the request is passed to `other`;
   * otherwise, if `this` returns a response, then `other` is not invoked.
   *
   * @param other fallback handler
   */
  def before(other: RequestHandler): RequestHandler =
    apply(_) match
      case req: HttpRequest  => other(req)
      case res: HttpResponse => res

  /**
   * Creates composite handler by applying `this` after `other`.
   *
   * If `other` returns a request, then the request is passed to `this`;
   * otherwise, if `other` returns a response, then `this` is not invoked.
   *
   * @param other initial handler
   */
  def after(other: RequestHandler): RequestHandler =
    other(_) match
      case req: HttpRequest  => apply(req)
      case res: HttpResponse => res

/** Provides `RequestHandler` utilities. */
object RequestHandler:
  /**
   * Composes request handlers by applying handlers in order until either
   * request is satisfied or handlers are exhausted.
   *
   * @param handlers request handlers
   *
   * @note If `handlers` is empty, the returned handler simply returns supplied
   * request.
   */
  def coalesce(handlers: Seq[RequestHandler]): RequestHandler =
    @annotation.tailrec
    def handle(req: HttpRequest, handlers: Seq[RequestHandler]): HttpMessage =
      handlers match
        case Nil          => req
        case head +: tail =>
          head(req) match
            case req: HttpRequest  => handle(req, tail)
            case res: HttpResponse => res
    handle(_, handlers)

  /**
   * Composes request handlers by applying handlers in order until either
   * request is satisfied or handlers are exhausted.
   *
   * @param one request handler
   * @param more additional request handlers
   */
  def coalesce(one: RequestHandler, more: RequestHandler*): RequestHandler =
    coalesce(one +: more)
