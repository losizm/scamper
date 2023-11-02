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

/** Provides standardized access to Max-Forwards header. */
given toMaxForwards: Conversion[HttpRequest, MaxForwards] = MaxForwards(_)

/** Provides standardized access to Max-Forwards header. */
class MaxForwards(request: HttpRequest) extends AnyVal:
  /** Tests for Max-Forwards header. */
  def hasMaxForwards: Boolean =
    request.hasHeader("Max-Forwards")

  /**
   * Gets Max-Forwards header value.
   *
   * @throws HeaderNotFound if Max-Forwards is not present
   */
  def maxForwards: Long =
    maxForwardsOption.getOrElse(throw HeaderNotFound("Max-Forwards"))

  /** Gets Max-Forwards header value if present. */
  def maxForwardsOption: Option[Long] =
    request.getHeader("Max-Forwards").map(_.longValue)

  /** Creates new request with Max-Forwards header set to supplied value. */
  def setMaxForwards(value: Long): HttpRequest =
    request.putHeaders(Header("Max-Forwards", value))

  /** Creates new request with Max-Forwards header removed. */
  def maxForwardsRemoved: HttpRequest =
    request.removeHeaders("Max-Forwards")
