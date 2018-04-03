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

/**
 * Standardized type for Accept-Language header value.
 *
 * @see [[scamper.ImplicitHeaders.AcceptLanguage]]
 */
trait LanguageRange {
  /** Language tag */
  def tag: String

  /** Language weight */
  def weight: Float

  /** Tests whether language tag is wildcard (*). */
  def isWildcard: Boolean = tag == "*"

  /** Tests whether supplied language tag matches range. */
  def matches(tag: LanguageTag): Boolean

  /** Returns formatted language range. */
  override lazy val toString: String = tag + "; q=" + weight
}

/** LanguageRange factory */
object LanguageRange {
  private val syntax = """([\w*-]+)(?i:\s*;\s*q=(\d+(?:\.\d*)?))?""".r

  /** Parses formatted language range. */
  def apply(range: String): LanguageRange =
    range match {
      case syntax(tag, null)   => apply(tag, 1.0f)
      case syntax(tag, weight) => apply(tag, weight.toFloat)
      case _ => throw new IllegalArgumentException(s"Malformed language range: $range")
    }

  /** Creates LanguageRange with supplied language tag and weight. */
  def apply(tag: String, weight: Float): LanguageRange =
    LanguageRangeImpl(tag, QValue(weight))

  /** Destructures LanguageRange. */
  def unapply(range: LanguageRange): Option[(String, Float)] =
    Some((range.tag, range.weight))
}

private case class LanguageRangeImpl(tag: String, weight: Float) extends LanguageRange {
  private val languageTag = if (tag == "*") None else Some(LanguageTag(tag))

  def matches(that: LanguageTag): Boolean =
    languageTag.forall { tag =>
      tag.primary.equalsIgnoreCase(that.primary) && matchesOthers(tag.others, that.others)
    }

  private def matchesOthers(others: Seq[String], that: Seq[String]): Boolean =
    others.size <= that.size && others.zip(that).forall(x => x._1.equalsIgnoreCase(x._2))
}