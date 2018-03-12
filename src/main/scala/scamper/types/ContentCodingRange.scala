package scamper.types

import CodingHelper.Name

/**
 * Standardized type for Accept-Encoding header value.
 *
 * @see [[scamper.headers.AcceptEncoding]]
 */
trait ContentCodingRange {
  /** Coding name */
  def name: String

  /** Coding weight */
  def weight: Float

  /** Tests whether name is compress. */
  def isCompress: Boolean = name == "compress"

  /** Tests whether name is deflate. */
  def isDeflate: Boolean = name == "deflate"

  /** Tests whether name is gzip. */
  def isGzip: Boolean = name == "gzip"

  /** Tests whether name is identity. */
  def isIdentity: Boolean = name == "identity"

  /** Tests whether name is wildcard (*). */
  def isWildcard: Boolean = name == "*"

  /** Tests whether supplied content coding matches range. */
  def matches(coding: ContentCoding): Boolean

  /** Returns formatted content coding range. */
  override lazy val toString: String =
    name + "; q=" + weight
}

/** ContentCodingRange factory */
object ContentCodingRange {
  private val syntax = """([^\s;=]+)(?:\s*;\s*q\s*=\s*(\d+(?:\.\d*)?))?""".r

  /** Parses formatted content coding range. */
  def apply(range: String): ContentCodingRange =
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
    isWildcard || name.equalsIgnoreCase(coding.name)
}

