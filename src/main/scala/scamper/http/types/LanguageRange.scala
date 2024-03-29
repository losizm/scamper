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

/**
 * Standardized type for Accept-Language header value.
 *
 * @see [[scamper.http.headers.AcceptLanguage]]
 */
trait LanguageRange:
  /** Gets language tag. */
  def tag: String

  /** Gets language weight. */
  def weight: Float

  /** Tests for wildcard (*). */
  def isWildcard: Boolean = tag == "*"

  /** Tests whether range matches supplied language tag. */
  def matches(tag: LanguageTag): Boolean

  /** Returns formatted range. */
  override lazy val toString: String =
    if weight == 1.0f then tag
    else tag + "; q=" + weight

/** Provides factory for `LanguageRange`. */
object LanguageRange:
  private val syntax = """([\w*-]+)(?i:\s*;\s*q=(\d+(?:\.\d*)?))?""".r

  /** Parses formatted range. */
  def parse(range: String): LanguageRange =
    range match
      case syntax(tag, null)   => apply(tag, 1.0f)
      case syntax(tag, weight) => apply(tag, weight.toFloat)
      case _ => throw IllegalArgumentException(s"Malformed language range: $range")

  /** Creates range with supplied language tag and weight. */
  def apply(tag: String, weight: Float): LanguageRange =
    LanguageRangeImpl(tag, QValue(weight))

private case class LanguageRangeImpl(tag: String, weight: Float) extends LanguageRange:
  private val languageTag = if tag == "*" then None else Some(LanguageTag.parse(tag))

  def matches(that: LanguageTag): Boolean =
    languageTag.forall { tag =>
      tag.primary.equalsIgnoreCase(that.primary) && matchesOthers(tag.others, that.others)
    } && weight != 0

  private def matchesOthers(others: Seq[String], that: Seq[String]): Boolean =
    others.size <= that.size && others.zip(that).forall(x => x._1.equalsIgnoreCase(x._2))
