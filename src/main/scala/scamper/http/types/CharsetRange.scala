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

import Grammar.Token

/**
 * Standardized type for Accept-Charset header value.
 *
 * @see [[scamper.http.headers.AcceptCharset]]
 */
trait CharsetRange:
  /** Gets charset. */
  def charset: String

  /** Gets charset weight. */
  def weight: Float

  /** Tests for wildcard (*). */
  def isWildcard: Boolean = charset == "*"

  /** Tests whether range matches supplied charset. */
  def matches(charset: String): Boolean

  /** Returns formatted range. */
  override lazy val toString: String =
    if weight == 1.0f then charset
    else charset + "; q=" + weight

/** Provides factory for `CharsetRange`. */
object CharsetRange:
  private val syntax = """([^\s;=]+)(?:\s*;\s*q\s*=\s*(\d+(?:\.\d*)?))?""".r

  /** Parses formatted range. */
  def parse(range: String): CharsetRange =
    range match
      case syntax(charset, null) => apply(charset, 1.0f)
      case syntax(charset, weight) => apply(charset, weight.toFloat)
      case _ => throw IllegalArgumentException(s"Malformed charset range: $range")

  /** Creates range with supplied charset and weight. */
  def apply(charset: String, weight: Float): CharsetRange =
    Token(charset).map(charset => CharsetRangeImpl(charset, QValue(weight))).getOrElse {
      throw IllegalArgumentException(s"Invalid charset: $charset")
    }

private case class CharsetRangeImpl(charset: String, weight: Float) extends CharsetRange:
  def matches(that: String): Boolean =
    (isWildcard || charset.equalsIgnoreCase(that)) && weight > 0
