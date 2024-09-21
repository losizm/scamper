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

/** Defines filter for outgoing response. */
@FunctionalInterface
trait ResponseFilter:
  /**
   * Filters outgoing response.
   *
   * @param response outgoing response
   */
  def apply(response: HttpResponse): HttpResponse

  /**
   * Creates composite filter by applying `this` before `other`.
   *
   * @param other other filter
   */
  def before(other: ResponseFilter): ResponseFilter =
    res => other(apply(res))

  /**
   * Creates composite filter by applying `this` after `other`.
   *
   * @param other other filter
   */
  def after(other: ResponseFilter): ResponseFilter =
    res => apply(other(res))

/** Provides `ResponseFilter` utilities. */
object ResponseFilter:
  /**
   * Composes response filters, with filters applied in order.
   *
   * @param filters response filters
   *
   * @note If `filters` is empty, a filter is created to return supplied
   * response.
   */
  def chain(filters: Seq[ResponseFilter]): ResponseFilter =
    @annotation.tailrec
    def filter(res: HttpResponse, filters: Seq[ResponseFilter]): HttpResponse =
      filters match
        case Nil          => res
        case head +: tail => filter(head(res), tail)
    filter(_, filters)

  /**
   * Composes response filters, with filters applied in order.
   *
   * @param one response filter
   * @param more additional response filters
   */
  def chain(one: ResponseFilter, more: ResponseFilter*): ResponseFilter =
    chain(one +: more)
