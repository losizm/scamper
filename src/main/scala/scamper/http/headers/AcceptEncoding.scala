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

import scamper.http.types.ContentCodingRange

/** Provides standardized access to Accept-Encoding header. */
given toAcceptEncoding: Conversion[HttpRequest, AcceptEncoding] = AcceptEncoding(_)

/** Provides standardized access to Accept-Encoding header. */
class AcceptEncoding(request: HttpRequest) extends AnyVal:
  /** Tests for Accept-Encoding header. */
  def hasAcceptEncoding: Boolean =
    request.hasHeader("Accept-Encoding")

  /**
   * Gets Accept-Encoding header values.
   *
   * @return header values or empty sequence if Accept-Encoding is not present
   */
  def acceptEncoding: Seq[ContentCodingRange] =
    acceptEncodingOption.getOrElse(Nil)

  /** Gets Accept-Encoding header values if present. */
  def acceptEncodingOption: Option[Seq[ContentCodingRange]] =
    request.getHeaderValue("Accept-Encoding")
      .map(ListParser.apply)
      .map(_.map(ContentCodingRange.parse))

  /** Creates new request with Accept-Encoding header set to supplied values. */
  def setAcceptEncoding(values: Seq[ContentCodingRange]): HttpRequest =
    request.putHeaders(Header("Accept-Encoding", values.mkString(", ")))

  /** Creates new request with Accept-Encoding header set to supplied values. */
  def setAcceptEncoding(one: ContentCodingRange, more: ContentCodingRange*): HttpRequest =
    setAcceptEncoding(one +: more)

  /** Creates new request with Accept-Encoding header removed. */
  def acceptEncodingRemoved: HttpRequest =
    request.removeHeaders("Accept-Encoding")
