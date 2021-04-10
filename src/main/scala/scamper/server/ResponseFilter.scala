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

import scamper.HttpResponse

/** Defines filter for outgoing response. */
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
  def chain(filters: Seq[ResponseFilter]): ResponseFilter = {
    @annotation.tailrec
    def filter(res: HttpResponse, filters: Seq[ResponseFilter]): HttpResponse =
      filters match {
        case Nil          => res
        case head +: tail => filter(head(res), tail)
      }
    filter(_, filters)
  }

  /**
   * Composes chain of response filters, with response of preceding filter
   * passed to its successor.
   *
   * @param one response filter
   * @param more additional response filters
   */
  def chain(one: ResponseFilter, more: ResponseFilter*): ResponseFilter =
    chain(one +: more)
}
