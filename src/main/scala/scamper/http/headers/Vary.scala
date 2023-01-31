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

/** Provides standardized access to Vary header. */
implicit class Vary(response: HttpResponse) extends AnyVal:
  /** Tests for Vary header. */
  def hasVary: Boolean =
    response.hasHeader("Vary")

  /**
   * Gets Vary header values.
   *
   * @return header values or empty sequence if Vary is not present
   */
  def vary: Seq[String] =
    varyOption.getOrElse(Nil)

  /** Gets Vary header values if present. */
  def varyOption: Option[Seq[String]] =
    response.getHeaderValue("Vary").map(ListParser.apply)

  /** Creates new response with Vary header set to supplied values. */
  def setVary(values: Seq[String]): HttpResponse =
    response.putHeaders(Header("Vary", values.mkString(", ")))

  /** Creates new response with Vary header set to supplied values. */
  def setVary(one: String, more: String*): HttpResponse =
    setVary(one +: more)

  /** Creates new response with Vary header removed. */
  def varyRemoved: HttpResponse =
    response.removeHeaders("Vary")
