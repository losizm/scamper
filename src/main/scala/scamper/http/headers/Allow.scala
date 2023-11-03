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

/** Adds standardized access to Allow header. */
given toAllow: Conversion[HttpResponse, Allow] = Allow(_)

/** Adds standardized access to Allow header. */
class Allow(response: HttpResponse) extends AnyVal:
  /** Tests for Allow header. */
  def hasAllow: Boolean =
    response.hasHeader("Allow")

  /**
   * Gets Allow header values.
   *
   * @return header values or empty sequence if Allow is not present
   */
  def allow: Seq[RequestMethod] =
    allowOption.getOrElse(Nil)

  /** Gets Allow header values if present. */
  def allowOption: Option[Seq[RequestMethod]] =
    response.getHeaderValue("Allow")
      .map(ListParser.apply)
      .map(_.map(RequestMethod.apply))

  /** Creates new response with Allow header set to supplied values. */
  def setAllow(values: Seq[RequestMethod]): HttpResponse =
    response.putHeaders(Header("Allow", values.mkString(", ")))

  /** Creates new response with Allow header set to supplied values. */
  def setAllow(one: RequestMethod, more: RequestMethod*): HttpResponse =
    setAllow(one +: more)

  /** Creates new response with Allow header removed. */
  def allowRemoved: HttpResponse =
    response.removeHeaders("Allow")
