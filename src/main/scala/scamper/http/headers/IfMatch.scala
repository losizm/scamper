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

/** Provides standardized access to If-Match header. */
implicit class IfMatch(request: HttpRequest) extends AnyVal:
  /** Tests for If-Match header. */
  def hasIfMatch: Boolean =
    request.hasHeader("If-Match")

  /**
   * Gets If-Match header values.
   *
   * @return header values or empty sequence if If-Match is not present
   */
  def ifMatch: Seq[EntityTag] =
    ifMatchOption.getOrElse(Nil)

  /** Gets If-Match header values if present. */
  def ifMatchOption: Option[Seq[EntityTag]] =
    request.getHeaderValue("If-Match")
      .map(ListParser.apply)
      .map(_.map(EntityTag.parse))

  /** Creates new request with If-Match header set to supplied values. */
  def setIfMatch(values: Seq[EntityTag]): HttpRequest =
    request.putHeaders(Header("If-Match", values.mkString(", ")))

  /** Creates new request with If-Match header set to supplied values. */
  def setIfMatch(one: EntityTag, more: EntityTag*): HttpRequest =
    setIfMatch(one +: more)

  /** Creates new request with If-Match header removed. */
  def ifMatchRemoved: HttpRequest =
    request.removeHeaders("If-Match")
