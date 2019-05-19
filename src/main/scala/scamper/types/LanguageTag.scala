/*
 * Copyright 2019 Carlos Conyers
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
 * Standardized type for Content-Language header value.
 *
 * @see [[scamper.headers.ContentLanguage]]
 */
trait LanguageTag {
  /** Gets primary subtag. */
  def primary: String

  /** Gets other subtags. */
  def others: Seq[String]

  /** Converts to LanguageRange with supplied weight. */ 
  def toRange(weight: Float): LanguageRange =
    LanguageRange(toString, weight)

  /** Returns formatted language tag. */
  override lazy val toString: String =
    primary + others.foldLeft("")((sum, it) => sum + "-" + it)
}

/** LanguageTag factory */
object LanguageTag {
  private val syntax = """(\p{Alpha}{1,8})((?:-\p{Alnum}{1,8})*)?""".r
  private val primary = "(\\p{Alpha}{1,8})".r
  private val other = "(\\p{Alnum}{1,8})".r

  /** Parses formatted language tag. */
  def parse(tag: String): LanguageTag =
    tag match {
      case syntax(primary, "")   => apply(primary, Nil)
      case syntax(primary, others) => apply(primary, others.drop(1).split("-").toSeq)
      case _ => throw new IllegalArgumentException(s"Malformed language tag: $tag")
    }

  /** Creates LanguageTag with primary subtag and additional subtags. */
  def apply(primary: String, others: Seq[String]): LanguageTag =
    LanguageTagImpl(Primary(primary), others.collect(Other))


  /** Destructures LanguageTag. */
  def unapply(tag: LanguageTag): Option[(String, Seq[String])] =
    Some((tag.primary, tag.others))

  private def Primary: PartialFunction[String, String] = {
    case primary(value) => value
    case value => throw new IllegalArgumentException(s"Invalid primary subtag: $value")
  }
  
  private def Other: PartialFunction[String, String] = {
    case other(value) => value
    case value => throw new IllegalArgumentException(s"Invalid subtag: $value")
  }
}

private case class LanguageTagImpl(primary: String, others: Seq[String]) extends LanguageTag
