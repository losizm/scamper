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

/** Provides standardized access to Expires header. */
implicit class Expires(response: HttpResponse) extends AnyVal:
  /** Tests for Expires header. */
  def hasExpires: Boolean =
    response.hasHeader("Expires")

  /**
   * Gets Expires header value.
   *
   * @throws HeaderNotFound if Expires is not present
   */
  def expires: Instant =
    getExpires.getOrElse(throw HeaderNotFound("Expires"))

  /** Gets Expires header value if present. */
  def getExpires: Option[Instant] =
    response.getHeader("Expires").map(_.instantValue)

  /** Creates new response with Expires header set to supplied value. */
  def setExpires(value: Instant): HttpResponse =
    response.putHeaders(Header("Expires", value))

  /** Creates new response with Expires header removed. */
  def removeExpires: HttpResponse =
    response.removeHeaders("Expires")
