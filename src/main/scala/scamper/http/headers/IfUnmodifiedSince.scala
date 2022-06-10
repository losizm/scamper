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

/** Provides standardized access to If-Unmodified-Since header. */
implicit class IfUnmodifiedSince(request: HttpRequest) extends AnyVal:
  /** Tests for If-Unmodified-Since header. */
  def hasIfUnmodifiedSince: Boolean =
    request.hasHeader("If-Unmodified-Since")

  /**
   * Gets If-Unmodified-Since header value.
   *
   * @throws HeaderNotFound if If-Unmodified-Since is not present
   */
  def ifUnmodifiedSince: Instant =
    getIfUnmodifiedSince.getOrElse(throw HeaderNotFound("If-Unmodified-Since"))

  /** Gets If-Unmodified-Since header value if present. */
  def getIfUnmodifiedSince: Option[Instant] =
    request.getHeader("If-Unmodified-Since").map(_.dateValue)

  /**
   * Creates new request with If-Unmodified-Since header set to supplied value.
   */
  def setIfUnmodifiedSince(value: Instant): HttpRequest =
    request.putHeaders(Header("If-Unmodified-Since", value))

  /** Creates new request with If-Unmodified-Since header removed. */
  def removeIfUnmodifiedSince: HttpRequest =
    request.removeHeaders("If-Unmodified-Since")