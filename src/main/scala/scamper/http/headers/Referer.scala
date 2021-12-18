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

/** Provides standardized access to Referer header. */
implicit class Referer(request: HttpRequest) extends AnyVal:
  /** Tests for Referer header. */
  def hasReferer: Boolean =
    request.hasHeader("Referer")

  /**
   * Gets Referer header value.
   *
   * @throws HeaderNotFound if Referer is not present
   */
  def referer: Uri =
    getReferer.getOrElse(throw HeaderNotFound("Referer"))

  /** Gets Referer header value if present. */
  def getReferer: Option[Uri] =
    request.getHeaderValue("Referer").map(Uri(_))

  /** Creates new request with Referer header set to supplied value. */
  def setReferer(value: Uri): HttpRequest =
    request.putHeaders(Header("Referer", value.toString))

  /** Creates new request with Referer header removed. */
  def removeReferer: HttpRequest =
    request.removeHeaders("Referer")
