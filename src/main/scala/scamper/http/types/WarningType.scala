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
package types

import java.time.Instant

/**
 * Standardized type for Warning header value.
 *
 * @see [[scamper.http.headers.Warning]]
 */
trait WarningType:
  /** Gets warning code. */
  def code: Int

  /** Gets warning agent. */
  def agent: String

  /** Gets warning text. */
  def text: String

  /** Gets warning date. */
  def date: Option[Instant]

  /** Returns formatted warning. */
  override lazy val toString: String =
    s"""$code $agent "$text"${date.map(x => " \"" + DateValue.format(x) + "\"").getOrElse("")}"""

/** Provides factory for `WarningType`. */
object WarningType:
  private val syntax = """\s*(\d{3})\s*([\p{Graph}&&[^",]]+)\s*"([^"]*)"\s*(?:"([\w, :+-]+)")?\s*""".r

  /** Parses formatted warning. */
  def parse(warning: String): WarningType =
    warning match
      case syntax(code, agent, text, null) => apply(code.toInt, agent, text)
      case syntax(code, agent, text, date) => apply(code.toInt, agent, text, Some(DateValue.parse(date)))
      case _ => throw IllegalArgumentException(s"Malformed warning: $warning")

  /** Parses formatted list of warnings. */
  def parseAll(warnings: String): Seq[WarningType] =
    ListParser(warnings).map(parse)

  /** Creates warning with supplied values. */
  def apply(code: Int, agent: String, text: String, date: Option[Instant] = None): WarningType =
    WarningTypeImpl(code, agent, text, date)

private case class WarningTypeImpl(code: Int, agent: String, text: String, date: Option[Instant]) extends WarningType
