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

import scamper.http.types.LanguageRange

/** Provides standardized access to Accept-Language header. */
implicit class AcceptLanguage(request: HttpRequest) extends AnyVal:
  /** Tests for Accept-Language header. */
  def hasAcceptLanguage: Boolean =
    request.hasHeader("Accept-Language")

  /**
   * Gets Accept-Language header values.
   *
   * @return header values or empty sequence if Accept-Language is not present
   */
  def acceptLanguage: Seq[LanguageRange] =
    acceptLanguageOption.getOrElse(Nil)

  /** Gets Accept-Language header values if present. */
  def acceptLanguageOption: Option[Seq[LanguageRange]] =
    request.getHeaderValue("Accept-Language")
      .map(ListParser.apply)
      .map(_.map(LanguageRange.parse))

  /** Creates new request with Accept-Language header set to supplied values. */
  def setAcceptLanguage(values: Seq[LanguageRange]): HttpRequest =
    request.putHeaders(Header("Accept-Language", values.mkString(", ")))

  /** Creates new request with Accept-Language header set to supplied values. */
  def setAcceptLanguage(one: LanguageRange, more: LanguageRange*): HttpRequest =
    setAcceptLanguage(one +: more)

  /** Creates new request with Accept-Language header removed. */
  def acceptLanguageRemoved: HttpRequest =
    request.removeHeaders("Accept-Language")
