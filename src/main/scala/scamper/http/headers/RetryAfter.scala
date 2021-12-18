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
package headers

import java.time.Instant

/** Provides standardized access to Retry-After header. */
implicit class RetryAfter(response: HttpResponse) extends AnyVal:
  /** Tests for Retry-After header. */
  def hasRetryAfter: Boolean =
    response.hasHeader("Retry-After")

  /**
   * Gets Retry-After header value.
   *
   * @throws HeaderNotFound if Retry-After is not present
   */
  def retryAfter: Instant =
    getRetryAfter.getOrElse(throw HeaderNotFound("Retry-After"))

  /** Gets Retry-After header value if present. */
  def getRetryAfter: Option[Instant] =
    response.getHeader("Retry-After").map(_.instantValue)

  /** Creates new response with Retry-After header set to supplied value. */
  def setRetryAfter(value: Instant): HttpResponse =
    response.putHeaders(Header("Retry-After", value))

  /** Creates new response with Retry-After header removed. */
  def removeRetryAfter: HttpResponse =
    response.removeHeaders("Retry-After")
