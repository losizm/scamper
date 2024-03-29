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

/** Adds standardized access to Early-Data header. */
given toEarlyData: Conversion[HttpRequest, EarlyData] = EarlyData(_)

/** Adds standardized access to Early-Data header. */
class EarlyData(request: HttpRequest) extends AnyVal:
  /** Tests for Early-Data header. */
  def hasEarlyData: Boolean =
    request.hasHeader("Early-Data")

  /**
   * Gets Early-Data header value.
   *
   * @throws HeaderNotFound if Early-Data is not present
   */
  def earlyData: Int =
    earlyDataOption.getOrElse(throw HeaderNotFound("Early-Data"))

  /** Gets Early-Data header value if present. */
  def earlyDataOption: Option[Int] =
    request.getHeaderValue("Early-Data").map(_.toInt)

  /** Creates new request with Early-Data header set to supplied value. */
  def setEarlyData(value: Int): HttpRequest =
    request.putHeaders(Header("Early-Data", value))

  /** Creates new request with Early-Data header removed. */
  def earlyDataRemoved: HttpRequest =
    request.removeHeaders("Early-Data")
