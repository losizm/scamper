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

import scamper.http.types.PragmaDirective

/** Adds standardized access to Pragma header. */
given toPragma: Conversion[HttpRequest, Pragma] = Pragma(_)

/** Adds standardized access to Pragma header. */
class Pragma(request: HttpRequest) extends AnyVal:
  /** Tests for Pragma header. */
  def hasPragma: Boolean =
    request.hasHeader("Pragma")

  /**
   * Gets Pragma header values.
   *
   * @return header values or empty sequence if Pragma is not present
   */
  def pragma: Seq[PragmaDirective] =
    pragmaOption.getOrElse(Nil)

  /** Gets Pragma header values if present. */
  def pragmaOption: Option[Seq[PragmaDirective]] =
    request.getHeaderValue("Pragma").map(PragmaDirective.parseAll)

  /** Creates new request with Pragma header set to supplied values. */
  def setPragma(values: Seq[PragmaDirective]): HttpRequest =
    request.putHeaders(Header("Pragma", values.mkString(", ")))

  /** Creates new request with Pragma header set to supplied values. */
  def setPragma(one: PragmaDirective, more: PragmaDirective*): HttpRequest =
    setPragma(one +: more)

  /** Creates new request with Pragma header removed. */
  def pragmaRemoved: HttpRequest =
    request.removeHeaders("Pragma")
