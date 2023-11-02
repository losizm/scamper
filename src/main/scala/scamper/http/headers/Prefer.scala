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

import scamper.http.types.Preference

/** Provides standardized access to Prefer header. */
given toPrefer: Conversion[HttpRequest, Prefer] = Prefer(_)

/** Provides standardized access to Prefer header. */
class Prefer(request: HttpRequest) extends AnyVal:
  /** Tests for Prefer header. */
  def hasPrefer: Boolean =
    request.hasHeader("Prefer")

  /**
   * Gets Prefer header values.
   *
   * @return header values or empty sequence if Prefer is not present
   */
  def prefer: Seq[Preference] =
    preferOption.getOrElse(Nil)

  /** Gets Prefer header values if present. */
  def preferOption: Option[Seq[Preference]] =
    request.getHeaderValues("Prefer")
      .flatMap(Preference.parseAll) match
        case Nil => None
        case seq => Some(seq)

  /** Creates new request with Prefer header set to supplied values. */
  def setPrefer(values: Seq[Preference]): HttpRequest =
    request.putHeaders(Header("Prefer", values.mkString(", ")))

  /** Creates new request with Prefer header set to supplied values. */
  def setPrefer(one: Preference, more: Preference*): HttpRequest =
    setPrefer(one +: more)

  /** Creates new request with Prefer header removed. */
  def preferRemoved: HttpRequest =
    request.removeHeaders("Prefer")
