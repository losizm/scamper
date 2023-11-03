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

import scamper.http.types.LinkType

/** Adds standardized access to Link header. */
given toLink: Conversion[HttpResponse, Link] = Link(_)

/** Adds standardized access to Link header. */
class Link(response: HttpResponse) extends AnyVal:
  /** Tests for Link header. */
  def hasLink: Boolean =
    response.hasHeader("Link")

  /**
   * Gets Link header values.
   *
   * @return header values or empty sequence if Link is not present
   */
  def link: Seq[LinkType] =
    linkOption.getOrElse(Nil)

  /** Gets Link header values if present. */
  def linkOption: Option[Seq[LinkType]] =
    response.getHeaderValue("Link").map(LinkType.parseAll)

  /** Creates new response with Link header set to supplied values. */
  def setLink(values: Seq[LinkType]): HttpResponse =
    response.putHeaders(Header("Link", values.mkString(", ")))

  /** Creates new response with Link header set to supplied values. */
  def setLink(one: LinkType, more: LinkType*): HttpResponse =
    setLink(one +: more)

  /** Creates new response with Link header removed. */
  def linkRemoved: HttpResponse =
    response.removeHeaders("Link")
