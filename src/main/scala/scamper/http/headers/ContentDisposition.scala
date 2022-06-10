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

import scamper.http.types.DispositionType

/** Provides standardized access to Content-Disposition header. */
implicit class ContentDisposition(response: HttpResponse) extends AnyVal:
  /** Tests for Content-Disposition header. */
  def hasContentDisposition: Boolean =
    response.hasHeader("Content-Disposition")

  /**
   * Gets Content-Disposition header value.
   *
   * @throws HeaderNotFound if Content-Disposition is not present
   */
  def contentDisposition: DispositionType =
    getContentDisposition.getOrElse(throw HeaderNotFound("Content-Disposition"))

  /** Gets Content-Disposition header value if present. */
  def getContentDisposition: Option[DispositionType] =
    response.getHeaderValue("Content-Disposition").map(DispositionType.parse)

  /** Creates new response with Content-Disposition header set to supplied value. */
  def setContentDisposition(value: DispositionType): HttpResponse =
    response.putHeaders(Header("Content-Disposition", value.toString))

  /** Creates new response with Content-Disposition header removed. */
  def removeContentDisposition: HttpResponse =
    response.removeHeaders("Content-Disposition")