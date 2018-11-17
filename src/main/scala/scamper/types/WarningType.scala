/*
 * Copyright 2018 Carlos Conyers
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
package scamper.types

import java.time.OffsetDateTime

import scamper.{ DateValue, ListParser }

/**
 * Standardized type for Warning header value.
 *
 * @see [[scamper.headers.Warning]]
 */
trait WarningType {
  /** Gets warning code. */
  def code: Int

  /** Gets warning agent. */
  def agent: String

  /** Gets warning text. */
  def text: String

  /** Gets warning date. */
  def date: Option[OffsetDateTime]

  /** Returns formatted warning. */
  override lazy val toString: String =
    code + " " + agent + " \"" + text + '"' + date.map(x => " \"" + DateValue.format(x) + '"').getOrElse("")
}

/** WarningType factory */
object WarningType {
  private val syntax = """\s*(\d{3})\s*([\p{Graph}&&[^",]]+)\s*"([^"]*)"\s*(?:"([\w, :+-]+)")?\s*""".r

  /** Parses formatted warning. */
  def parse(warning: String): WarningType =
    warning match {
      case syntax(code, agent, text, null) => apply(code.toInt, agent, text)
      case syntax(code, agent, text, date) => apply(code.toInt, agent, text, Some(DateValue.parse(date)))
      case _ => throw new IllegalArgumentException(s"Malformed warning: $warning")
    }

  /** Parses formatted list of warnings. */
  def parseAll(warnings: String): Seq[WarningType] =
    ListParser(warnings).map(parse)

  /** Creates WarningType with supplied values. */
  def apply(code: Int, agent: String, text: String, date: Option[OffsetDateTime] = None): WarningType =
    WarningTypeImpl(code, agent, text, date)

  /** Destructures WarningType. */
  def unapply(warning: WarningType): Option[(Int, String, String, Option[OffsetDateTime])] =
    Some((warning.code, warning.agent, warning.text, warning.date))
}

private case class WarningTypeImpl(code: Int, agent: String, text: String, date: Option[OffsetDateTime]) extends WarningType
