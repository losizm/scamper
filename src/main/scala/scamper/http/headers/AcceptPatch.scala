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

import scamper.http.types.MediaType

/** Provides standardized access to Accept-Patch header. */
implicit class AcceptPatch(response: HttpResponse) extends AnyVal:
  /** Tests for Accept-Patch header. */
  def hasAcceptPatch: Boolean =
    response.hasHeader("Accept-Patch")

  /**
   * Gets Accept-Patch header values.
   *
   * @return header values or empty sequence if Accept-Patch is not present
   */
  def acceptPatch: Seq[MediaType] =
    acceptPatchOption.getOrElse(Nil)

  /** Gets Accept-Patch header values if present. */
  def acceptPatchOption: Option[Seq[MediaType]] =
    response.getHeaderValue("Accept-Patch")
      .map(ListParser.apply)
      .map(_.map(MediaType.apply))

  /** Creates new response with Accept-Patch header set to supplied values. */
  def setAcceptPatch(values: Seq[MediaType]): HttpResponse =
    response.putHeaders(Header("Accept-Patch", values.mkString(", ")))

  /** Creates new response with Accept-Patch header set to supplied values. */
  def setAcceptPatch(one: MediaType, more: MediaType*): HttpResponse =
    setAcceptPatch(one +: more)

  /** Creates new response with Accept-Patch header removed. */
  def acceptPatchRemoved: HttpResponse =
    response.removeHeaders("Accept-Patch")
