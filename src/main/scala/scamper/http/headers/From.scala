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

/** Adds standardized access to From header. */
given toFrom: Conversion[HttpRequest, From] = From(_)

/** Adds standardized access to From header. */
class From(request: HttpRequest) extends AnyVal:
  /** Tests for From header. */
  def hasFrom: Boolean =
    request.hasHeader("From")

  /**
   * Gets From header value.
   *
   * @throws HeaderNotFound if From is not present
   */
  def from: String =
    fromOption.getOrElse(throw HeaderNotFound("From"))

  /** Gets From header value if present. */
  def fromOption: Option[String] =
    request.getHeaderValue("From")

  /** Creates new request with From header set to supplied value. */
  def setFrom(value: String): HttpRequest =
    request.putHeaders(Header("From", value))

  /** Creates new request with From header removed. */
  def fromRemoved: HttpRequest =
    request.removeHeaders("From")
