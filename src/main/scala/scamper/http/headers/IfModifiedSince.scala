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

/** Provides standardized access to If-Modified-Since header. */
implicit class IfModifiedSince(request: HttpRequest) extends AnyVal:
  /** Tests for If-Modified-Since header. */
  def hasIfModifiedSince: Boolean =
    request.hasHeader("If-Modified-Since")

  /**
   * Gets If-Modified-Since header value.
   *
   * @throws HeaderNotFound if If-Modified-Since is not present
   */
  def ifModifiedSince: Instant =
    ifModifiedSinceOption.getOrElse(throw HeaderNotFound("If-Modified-Since"))

  /** Gets If-Modified-Since header value if present. */
  def ifModifiedSinceOption: Option[Instant] =
    request.getHeader("If-Modified-Since").map(_.instantValue)

  /** Creates new request with If-Modified-Since header set to supplied value. */
  def setIfModifiedSince(value: Instant): HttpRequest =
    request.putHeaders(Header("If-Modified-Since", value))

  /** Creates new request with If-Modified-Since header removed. */
  def ifModifiedSinceRemoved: HttpRequest =
    request.removeHeaders("If-Modified-Since")
