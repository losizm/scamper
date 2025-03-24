/*
 * Copyright 2025 Carlos Conyers
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

import java.util.function.Predicate

/** Defines request predicate. */
@FunctionalInterface
trait RequestPredicate:
  /**
   * Tests request.
   *
   * @param request incoming request
   */
  def test(request: HttpRequest): Boolean

  /**
   * Creates predicate as logical `this` AND `other`.
   *
   * @param other predicate
   */
  def and(other: RequestPredicate): RequestPredicate =
    notNull(other)
    req => test(req) && other.test(req)

  /**
   * Creates predicate as logical `this` OR `other`.
   *
   * @param other predicate
   */
  def or(other: RequestPredicate): RequestPredicate =
    notNull(other)
    req => test(req) || other.test(req)

  /** Gets negated predicate. */
  def negate(): RequestPredicate =
    req => !test(req)

/** Provides `RequestPredicate` utilities. */
object RequestPredicate:
  /**
   * Combines request predicates for which all predicates must be satisfied.
   *
   * @param predicates request predicates
   *
   * @note If `predicates` is empty, the returned predicate is always satisfied.
   */
  def combine(predicates: Seq[RequestPredicate]): RequestPredicate =
    req => predicates.forall(_.test(req))

  /**
   * Combines request predicates for which all predicates must be satisfied.
   *
   * @param one request predicate
   * @param more additional request predicates
   */
  def combine(one: RequestPredicate, more: RequestPredicate*): RequestPredicate =
    combine(one +: more)
