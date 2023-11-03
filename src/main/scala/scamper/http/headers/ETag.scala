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

import scamper.http.types.EntityTag

/** Adds standardized access to ETag header. */
given toETag: Conversion[HttpResponse, ETag] = ETag(_)

/** Adds standardized access to ETag header. */
class ETag(response: HttpResponse) extends AnyVal:
  /** Tests for ETag header. */
  def hasETag: Boolean =
    response.hasHeader("ETag")

  /**
   * Gets ETag header value.
   *
   * @throws HeaderNotFound if ETag is not present
   */
  def eTag: EntityTag =
    eTagOption.getOrElse(throw HeaderNotFound("ETag"))

  /** Gets ETag header value if present. */
  def eTagOption: Option[EntityTag] =
    response.getHeaderValue("ETag").map(EntityTag.parse)

  /** Creates new response with ETag header set to supplied value. */
  def setETag(value: EntityTag): HttpResponse =
    response.putHeaders(Header("ETag", value.toString))

  /** Creates new response with ETag header removed. */
  def eTagRemoved: HttpResponse =
    response.removeHeaders("ETag")
