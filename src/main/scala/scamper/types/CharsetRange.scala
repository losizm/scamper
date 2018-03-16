package scamper.types

import scamper.Grammar.Token

/**
 * Standardized type for Accept-Charset header value.
 *
 * @see [[scamper.ImplicitHeaders.AcceptCharset]]
 */
trait CharsetRange {
  /** Charset */
  def charset: String

  /** Charset weight */
  def weight: Float

  /** Tests whether charset is wildcard (*). */
  def isWildcard: Boolean = charset == "*"

  /** Tests whether supplied charset matches range. */
  def matches(charset: String): Boolean

  /** Returns formatted charset range. */
  override lazy val toString: String =
    charset + "; q=" + weight
}

/** CharsetRange factory */
object CharsetRange {
  private val syntax = """([^\s;=]+)(?:\s*;\s*q\s*=\s*(\d+(?:\.\d*)?))?""".r

  /** Parses formatted charset range. */
  def apply(range: String): CharsetRange =
    range match {
      case syntax(charset, null) => apply(charset, 1.0f)
      case syntax(charset, weight) => apply(charset, weight.toFloat)
      case _ => throw new IllegalArgumentException(s"Malformed charset range: $range")
    }

  /** Creates CharsetRange with supplied charset and weight. */
  def apply(charset: String, weight: Float): CharsetRange =
    Token(charset).map(charset => CharsetRangeImpl(charset, QValue(weight))).getOrElse {
      throw new IllegalArgumentException(s"Invalid charset: $charset")
    }

  /** Destructures CharsetRange. */
  def unapply(range: CharsetRange): Option[(String, Float)] =
    Some((range.charset, range.weight))
}

private case class CharsetRangeImpl(charset: String, weight: Float) extends CharsetRange {
  def matches(that: String): Boolean =
    isWildcard || charset.equalsIgnoreCase(that)
}

