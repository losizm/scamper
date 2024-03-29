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

import scamper.http.types.ByteRange

/** Adds standardized access to Range header. */
given toRange: Conversion[HttpRequest, Range] = Range(_)

/** Adds standardized access to Range header. */
class Range(request: HttpRequest) extends AnyVal:
  /** Tests for Range header. */
  def hasRange: Boolean =
    request.hasHeader("Range")

  /**
   * Gets Range header value.
   *
   * @throws HeaderNotFound if Range is not present
   */
  def range: ByteRange =
    rangeOption.getOrElse(throw HeaderNotFound("Range"))

  /** Gets Range header value if present. */
  def rangeOption: Option[ByteRange] =
    request.getHeaderValue("Range").map(ByteRange.parse)

  /** Creates new request with Range header set to supplied value. */
  def setRange(value: ByteRange): HttpRequest =
    request.putHeaders(Header("Range", value.toString))

  /** Creates new request with Range header removed. */
  def rangeRemoved: HttpRequest =
    request.removeHeaders("Range")
