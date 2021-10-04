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

/** Defines utility for handling error during request processing. */
@FunctionalInterface
trait ErrorHandler:
  /**
   * Creates response for given error.
   *
   * @param request request for which error was generated
   */
  def apply(request: HttpRequest): PartialFunction[Throwable, HttpResponse]

  /**
   * Creates composite handler by applying `this` before `other`.
   *
   * If `this` is not defined, then error is applied to `other`.
   *
   * @param other fallback handler
   */
  def before(other: ErrorHandler): ErrorHandler =
    req => apply(req).orElse(other(req))

  /**
   * Creates composite handler by applying `this` after `other`.
   *
   * If `other` is not defined, then error is applied to `this`.
   *
   * @param other initial handler
   */
  def after(other: ErrorHandler): ErrorHandler =
    req => other(req).orElse(apply(req))

/** Provides `ErrorHandler` utilities. */
object ErrorHandler:
  private val empty = new ErrorHandler:
    def apply(req: HttpRequest) = PartialFunction.empty

  /**
   * Composes error handlers using tail handlers as fallbacks.
   *
   * @param handlers error handlers
   */
  def coalesce(handlers: Seq[ErrorHandler]): ErrorHandler =
    handlers.foldRight(empty)(_ before _)

  /**
   * Composes `one` handler with `more` handlers using `more` as fallbacks.
   *
   * @param one error handler
   * @param more additional error handlers
   */
  def coalesce(one: ErrorHandler, more: ErrorHandler*): ErrorHandler =
    coalesce(one +: more)
