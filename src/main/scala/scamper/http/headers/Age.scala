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

/** Provides standardized access to Age header. */
implicit class Age(response: HttpResponse) extends AnyVal:
  /** Tests for Age header. */
  def hasAge: Boolean =
    response.hasHeader("Age")

  /**
   * Gets Age header value.
   *
   * @throws HeaderNotFound if Age is not present
   */
  def age: Long =
    getAge.getOrElse(throw HeaderNotFound("Age"))

  /** Gets Age header value if present. */
  def getAge: Option[Long] =
    response.getHeader("Age").map(_.longValue)

  /** Creates new response with Age header set to supplied value. */
  def setAge(value: Long): HttpResponse =
    response.putHeaders(Header("Age", value))

  /** Creates new response with Age header removed. */
  def removeAge: HttpResponse =
    response.removeHeaders("Age")
