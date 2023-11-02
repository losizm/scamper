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

import scamper.http.types.WarningType

/** Provides standardized access to Warning header. */
given toWarning[T <: HttpMessage]: Conversion[T, Warning[T]] = Warning(_)

/** Provides standardized access to Warning header. */
class Warning[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Warning header. */
  def hasWarning: Boolean =
    message.hasHeader("Warning")

  /**
   * Gets Warning header values.
   *
   * @return header values or empty sequence if Warning is not present
   */
  def warning: Seq[WarningType] =
    warningOption.getOrElse(Nil)

  /** Gets Warning header values if present. */
  def warningOption: Option[Seq[WarningType]] =
    message.getHeaderValue("Warning").map(WarningType.parseAll)

  /** Creates new message with Warning header set to supplied values. */
  def setWarning(values: Seq[WarningType]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Warning", values.mkString(", ")))

  /** Creates new message with Warning header set to supplied values. */
  def setWarning(one: WarningType, more: WarningType*): T =
    setWarning(one +: more)

  /** Creates new message with Warning header removed. */
  def warningRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Warning")
