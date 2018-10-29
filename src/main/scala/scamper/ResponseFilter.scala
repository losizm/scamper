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

/** Provides utility for filtering HTTP response. */
trait ResponseFilter {
  /** Tests whether response matches filter condition. */
  def apply(response: HttpResponse): Boolean

  /**
   * Returns {@code Some(response)} if response matches filter condition,
   * and {@code None} otherwise.
   */
  def unapply(response: HttpResponse): Option[HttpResponse] =
    if (apply(response)) Some(response) else None
}

/** Includes status-based {@code ResponseFilter} implementations. */
object ResponseFilters {
  /**
   * Filters informational responses.
   *
   * See [[ResponseStatus.isInformational]].
   */
  val Informational: ResponseFilter =
    res => res.status.isInformational

  /**
   * Filters successful responses.
   *
   * See [[ResponseStatus.isSuccessful]].
   */
  val Successful: ResponseFilter =
    res => res.status.isSuccessful

  /**
   * Filters redirection responses.
   *
   * See [[ResponseStatus.isRedirection]].
   */
  val Redirection: ResponseFilter =
    res => res.status.isRedirection

  /**
   * Filters client error responses.
   *
   * See [[ResponseStatus.isClientError]].
   */
  val ClientError: ResponseFilter =
    res => res.status.isClientError

  /**
   * Filters server error responses.
   *
   * See [[ResponseStatus.isServerError]].
   */
  val ServerError: ResponseFilter =
    res => res.status.isServerError
}
