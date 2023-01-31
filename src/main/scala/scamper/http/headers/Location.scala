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

/** Provides standardized access to Location header. */
implicit class Location(response: HttpResponse) extends AnyVal:
  /** Tests for Location header. */
  def hasLocation: Boolean =
    response.hasHeader("Location")

  /**
   * Gets Location header value.
   *
   * @throws HeaderNotFound if Location is not present
   */
  def location: Uri =
    locationOption.getOrElse(throw HeaderNotFound("Location"))

  /** Gets Location header value if present. */
  def locationOption: Option[Uri] =
    response.getHeaderValue("Location").map(Uri(_))

  /** Creates new response with Location header set to supplied value. */
  def setLocation(value: Uri): HttpResponse =
    response.putHeaders(Header("Location", value.toString))

  /** Creates new response with Location header removed. */
  def locationRemoved: HttpResponse =
    response.removeHeaders("Location")
