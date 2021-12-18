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

import scamper.http.types.MediaRange

/** Provides standardized access to Accept header. */
implicit class Accept(request: HttpRequest) extends AnyVal:
  /** Tests for Accept header. */
  def hasAccept: Boolean =
    request.hasHeader("Accept")

  /**
   * Gets Accept header values.
   *
   * @return header values or empty sequence if Accept is not present
   */
  def accept: Seq[MediaRange] =
    getAccept.getOrElse(Nil)

  /** Gets Accept header values if present. */
  def getAccept: Option[Seq[MediaRange]] =
    request.getHeaderValue("Accept")
      .map(ListParser.apply)
      .map(_.map(MediaRange.apply))

  /** Creates new request with Accept header set to supplied values. */
  def setAccept(values: Seq[MediaRange]): HttpRequest =
    request.putHeaders(Header("Accept", values.mkString(", ")))

  /** Creates new request with Accept header set to supplied values. */
  def setAccept(one: MediaRange, more: MediaRange*): HttpRequest =
    setAccept(one +: more)

  /** Creates new request with Accept header removed. */
  def removeAccept: HttpRequest =
    request.removeHeaders("Accept")
