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

import scamper.http.types.TransferCodingRange

/** Provides standardized access to TE header. */
given toTE: Conversion[HttpRequest, TE] = TE(_)

/** Provides standardized access to TE header. */
class TE(request: HttpRequest) extends AnyVal:
  /** Tests for TE header. */
  def hasTE: Boolean =
    request.hasHeader("TE")

  /**
   * Gets TE header values.
   *
   * @return header values or empty sequence if TE is not present
   */
  def te: Seq[TransferCodingRange] =
    teOption.getOrElse(Nil)

  /** Gets TE header values if present. */
  def teOption: Option[Seq[TransferCodingRange]] =
    request.getHeaderValue("TE")
      .map(ListParser.apply)
      .map(_.map(TransferCodingRange.parse))

  /** Creates new request with TE header set to supplied values. */
  def setTE(values: Seq[TransferCodingRange]): HttpRequest =
    request.putHeaders(Header("TE", values.mkString(", ")))

  /** Creates new request with TE header set to supplied values. */
  def setTE(one: TransferCodingRange, more: TransferCodingRange*): HttpRequest =
    setTE(one +: more)

  /** Creates new request with TE header removed. */
  def teRemoved: HttpRequest =
    request.removeHeaders("TE")
