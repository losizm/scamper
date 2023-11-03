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

/** Adds standardized access to Last-Modified header. */
given toLastModified: Conversion[HttpResponse, LastModified] = LastModified(_)

/** Adds standardized access to Last-Modified header. */
class LastModified(response: HttpResponse) extends AnyVal:
  /** Tests for Last-Modified header. */
  def hasLastModified: Boolean =
    response.hasHeader("Last-Modified")

  /**
   * Gets Last-Modified header value.
   *
   * @throws HeaderNotFound if Last-Modified is not present
   */
  def lastModified: Instant =
    lastModifiedOption.getOrElse(throw HeaderNotFound("Last-Modified"))

  /** Gets Last-Modified header value if present. */
  def lastModifiedOption: Option[Instant] =
    response.getHeader("Last-Modified").map(_.instantValue)

  /** Creates new response with Last-Modified header set to supplied value. */
  def setLastModified(value: Instant): HttpResponse =
    response.putHeaders(Header("Last-Modified", value))

  /** Creates new response with Last-Modified header removed. */
  def lastModifiedRemoved: HttpResponse =
    response.removeHeaders("Last-Modified")
