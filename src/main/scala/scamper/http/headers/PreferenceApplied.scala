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

/** Provides standardized access to Preference-Applied header. */
given toPreferenceApplied: Conversion[HttpResponse, PreferenceApplied] = PreferenceApplied(_)

/** Provides standardized access to Preference-Applied header. */
class PreferenceApplied(response: HttpResponse) extends AnyVal:
  /** Tests for Preference-Applied header. */
  def hasPreferenceApplied: Boolean =
    response.hasHeader("Preference-Applied")

  /**
   * Gets Preference-Applied header values.
   *
   * @return header values or empty sequence if Preference-Applied is not present
   */
  def preferenceApplied: Seq[Preference] =
    preferenceAppliedOption.getOrElse(Nil)

  /** Gets Preference-Applied header values if present. */
  def preferenceAppliedOption: Option[Seq[Preference]] =
    response.getHeaderValues("Preference-Applied")
      .flatMap(Preference.parseAll) match
        case Nil => None
        case seq => Some(seq)

  /** Creates new response with Preference-Applied header set to supplied values. */
  def setPreferenceApplied(values: Seq[Preference]): HttpResponse =
    response.putHeaders(Header("Preference-Applied", values.mkString(", ")))

  /** Creates new response with Preference-Applied header set to supplied values. */
  def setPreferenceApplied(one: Preference, more: Preference*): HttpResponse =
    setPreferenceApplied(one +: more)

  /** Creates new response with Preference-Applied header removed. */
  def preferenceAppliedRemoved: HttpResponse =
    response.removeHeaders("Preference-Applied")
