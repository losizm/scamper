/*
 * Copyright 2017-2020 Carlos Conyers
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

import CodingHelper.Name

/**
 * Standardized type for Accept-Encoding header value.
 *
 * @see [[scamper.headers.AcceptEncoding]]
 */
trait ContentCodingRange {
  /** Gets coding name. */
  def name: String

  /** Gets coding weight. */
  def weight: Float

  /** Tests for compress. */
  def isCompress: Boolean = name == "compress"

  /** Tests for deflate. */
  def isDeflate: Boolean = name == "deflate"

  /** Tests for gzip. */
  def isGzip: Boolean = name == "gzip"

  /** Tests for identity. */
  def isIdentity: Boolean = name == "identity"

  /** Tests for wildcard (*). */
  def isWildcard: Boolean = name == "*"

  /** Tests whether range matches supplied content coding. */
  def matches(coding: ContentCoding): Boolean

  /** Returns formatted content coding range. */
  override lazy val toString: String =
    if (weight == 1.0f) name
    else name + "; q=" + weight
}

/** Provides factory methods for `ContentCodingRange`. */
object ContentCodingRange {
  private val syntax = """([^\s;=]+)(?:\s*;\s*q\s*=\s*(\d+(?:\.\d*)?))?""".r

  /** Parses formatted content coding range. */
  def parse(range: String): ContentCodingRange =
    range match {
      case syntax(name, null) => apply(name, 1.0f)
      case syntax(name, weight) => apply(name, weight.toFloat)
      case _ => throw new IllegalArgumentException(s"Malformed content coding range: $range")
    }

  /** Creates ContentCodingRange with supplied name and weight. */
  def apply(name: String, weight: Float): ContentCodingRange =
    ContentCodingRangeImpl(Name(name), QValue(weight))

  /** Destructures ContentCodingRange. */
  def unapply(range: ContentCodingRange): Option[(String, Float)] =
    Some((range.name, range.weight))
}

private case class ContentCodingRangeImpl(name: String, weight: Float) extends ContentCodingRange {
  def matches(coding: ContentCoding): Boolean =
    (isWildcard || name.equalsIgnoreCase(coding.name)) && weight > 0
}
