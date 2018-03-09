package scamper.types

/**
 * Standardized type for Accept-Language header value.
 *
 * @see [[scamper.headers.AcceptLanguage]]
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
    new LanguageRangeImpl(tag, QValue(weight))

  /** Destructures LanguageRange. */
  def unapply(range: LanguageRange): Option[(String, Float)] =
    Some((range.tag, range.weight))
}

private class LanguageRangeImpl(val tag: String, val weight: Float) extends LanguageRange {
  private val languageTag = if (tag == "*") None else Some(LanguageTag(tag))

  def matches(that: LanguageTag): Boolean =
    languageTag.map { tag =>
      tag.primary.equalsIgnoreCase(that.primary) && matchesOthers(tag.others, that.others)
    }.getOrElse(true)

  private def matchesOthers(others: Seq[String], that: Seq[String]): Boolean =
    others.size <= that.size && others.zip(that).forall(x => x._1.equalsIgnoreCase(x._2))
}

