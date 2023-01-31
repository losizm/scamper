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

/** Provides standardized access to Expect header. */
implicit class Expect(request: HttpRequest) extends AnyVal:
  /** Tests for Expect header. */
  def hasExpect: Boolean =
    request.hasHeader("Expect")

  /**
   * Gets Expect header value.
   *
   * @throws HeaderNotFound if Expect is not present
   */
  def expect: String =
    expectOption.getOrElse(throw HeaderNotFound("Expect"))

  /** Gets Expect header value if present. */
  def expectOption: Option[String] =
    request.getHeaderValue("Expect")

  /** Creates new request with Expect header set to supplied value. */
  def setExpect(value: String): HttpRequest =
    request.putHeaders(Header("Expect", value))

  /** Creates new request with Expect header removed. */
  def expectRemoved: HttpRequest =
    request.removeHeaders("Expect")
