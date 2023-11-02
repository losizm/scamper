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

import scamper.http.types.EntityTag

/** Provides standardized access to If-None-Match header. */
given toIfNoneMatch: Conversion[HttpRequest, IfNoneMatch] = IfNoneMatch(_)

/** Provides standardized access to If-None-Match header. */
class IfNoneMatch(request: HttpRequest) extends AnyVal:
  /** Tests for If-None-Match header. */
  def hasIfNoneMatch: Boolean =
    request.hasHeader("If-None-Match")

  /**
   * Gets If-None-Match header values.
   *
   * @return header values or empty sequence if If-None-Match is not present
   */
  def ifNoneMatch: Seq[EntityTag] =
    ifNoneMatchOption.getOrElse(Nil)

  /** Gets If-None-Match header values if present. */
  def ifNoneMatchOption: Option[Seq[EntityTag]] =
    request.getHeaderValue("If-None-Match")
      .map(ListParser.apply)
      .map(_.map(EntityTag.parse))

  /** Creates new request with If-None-Match header set to supplied values. */
  def setIfNoneMatch(values: Seq[EntityTag]): HttpRequest =
    request.putHeaders(Header("If-None-Match", values.mkString(", ")))

  /** Creates new request with If-None-Match header set to supplied values. */
  def setIfNoneMatch(one: EntityTag, more: EntityTag*): HttpRequest =
    setIfNoneMatch(one +: more)

  /** Creates new request with If-None-Match header removed. */
  def ifNoneMatchRemoved: HttpRequest =
    request.removeHeaders("If-None-Match")
