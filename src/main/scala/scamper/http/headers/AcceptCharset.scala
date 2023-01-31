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

import scamper.http.types.CharsetRange

/** Provides standardized access to Accept-Charset header. */
implicit class AcceptCharset(request: HttpRequest) extends AnyVal:
  /** Tests for Accept-Charset header. */
  def hasAcceptCharset: Boolean =
    request.hasHeader("Accept-Charset")

  /**
   * Gets Accept-Charset header values.
   *
   * @return header values or empty sequence if Accept-Charset is not present
   */
  def acceptCharset: Seq[CharsetRange] =
    acceptCharsetOption.getOrElse(Nil)

  /** Gets Accept-Charset header values if present. */
  def acceptCharsetOption: Option[Seq[CharsetRange]] =
    request.getHeaderValue("Accept-Charset")
      .map(ListParser.apply)
      .map(_.map(CharsetRange.parse))

  /** Creates new request with Accept-Charset header set to supplied values. */
  def setAcceptCharset(values: Seq[CharsetRange]): HttpRequest =
    request.putHeaders(Header("Accept-Charset", values.mkString(", ")))

  /** Creates new request with Accept-Charset header set to supplied values. */
  def setAcceptCharset(one: CharsetRange, more: CharsetRange*): HttpRequest =
    setAcceptCharset(one +: more)

  /** Creates new request with Accept-Charset header removed. */
  def acceptCharsetRemoved: HttpRequest =
    request.removeHeaders("Accept-Charset")
