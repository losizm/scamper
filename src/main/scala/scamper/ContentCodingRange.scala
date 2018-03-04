package scamper

import Grammar.Token

/**
 * Content Coding Range
 *
 * @see [[ImplicitHeaders.AcceptEncoding]]
 */
trait ContentCodingRange {
  /** Coding name */
  def name: String

  /** Coding weight */
  def weight: Float

  /** Tests whether name is compress. */
  def isCompress: Boolean = name == "compress" || name == "x-compress"

  /** Tests whether name is deflate. */
  def isDeflate: Boolean = name == "deflate"

  /** Tests whether name is gzip. */
  def isGzip: Boolean = name == "gzip" || name == "x-gzip"

  /** Tests whether name is identity. */
  def isIdentity: Boolean = name == "identity"

  /** Tests whether name is wildcard (*). */
  def isWildcard: Boolean = name == "*"

  /** Returns formatted content coding range. */
  override lazy val toString: String = {
    val range = new StringBuilder
    range.append(name)
    if (weight < 1f) range.append("; q=").append(weight)
    range.toString
  }
}

/** ContentCodingRange factory */
object ContentCodingRange {
  private val syntax = """(\p{Graph}+)(?:\s*;\s*q\s*=\s*(\d+(?:\.\d*)?))?""".r

  /** Parses formatted content coding range. */
  def apply(range: String): ContentCodingRange =
    range match {
      case syntax(name, null) => apply(name, 1.0f)
      case syntax(name, weight) => apply(name, weight.toFloat)
      case _ => throw new IllegalArgumentException(s"Malformed content coding range: $range")
    }

  /** Creates ContentCodingRange with supplied name and weight. */
  def apply(name: String, weight: Float): ContentCodingRange =
    Token(name).map(name => new ContentCodingRangeImpl(name.toLowerCase, QValue(weight))).getOrElse {
      throw new IllegalArgumentException(s"Invalid content coding name: $name")
    }

  /** Destructures ContentCodingRange. */
  def unapply(range: ContentCodingRange): Option[(String, Float)] =
    Some((range.name, range.weight))
}

private class ContentCodingRangeImpl(val name: String, val weight: Float) extends ContentCodingRange

