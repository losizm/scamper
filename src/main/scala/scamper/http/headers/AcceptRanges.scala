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

/** Provides standardized access to Accept-Ranges header. */
given toAcceptRanges: Conversion[HttpResponse, AcceptRanges] = AcceptRanges(_)

/** Provides standardized access to Accept-Ranges header. */
class AcceptRanges(response: HttpResponse) extends AnyVal:
  /** Tests for Accept-Ranges header. */
  def hasAcceptRanges: Boolean =
    response.hasHeader("Accept-Ranges")

  /**
   * Gets Accept-Ranges header values.
   *
   * @return header values or empty sequence if Accept-Ranges is not present
   */
  def acceptRanges: Seq[String] =
    acceptRangesOption.getOrElse(Nil)

  /** Gets Accept-Ranges header values if present. */
  def acceptRangesOption: Option[Seq[String]] =
    response.getHeaderValue("Accept-Ranges").map(ListParser.apply)

  /** Creates new response with Accept-Ranges header set to supplied values. */
  def setAcceptRanges(values: Seq[String]): HttpResponse =
    response.putHeaders(Header("Accept-Ranges", values.mkString(", ")))

  /** Creates new response with Accept-Ranges header set to supplied values. */
  def setAcceptRanges(one: String, more: String*): HttpResponse =
    setAcceptRanges(one +: more)

  /** Creates new response with Accept-Ranges header removed. */
  def acceptRangesRemoved: HttpResponse =
    response.removeHeaders("Accept-Ranges")
