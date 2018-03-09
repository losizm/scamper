package scamper.types

/**
 * Standardized type for Content-Language header value.
 *
 * @see [[scamper.headers.ContentLanguage]]
 */
trait LanguageTag {
  /** Primary subtag */
  def primary: String

  /** Other subtags */
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
  def apply(tag: String): LanguageTag =
    tag match {
      case syntax(primary, "")   => apply(primary, Nil)
      case syntax(primary, others) => apply(primary, others.drop(1).split("-").toSeq)
      case _ => throw new IllegalArgumentException(s"Malformed language tag: $tag")
    }

  /** Creates LanguageTag with primary subtag and additional subtags. */
  def apply(primary: String, others: Seq[String]): LanguageTag =
    new LanguageTagImpl(Primary(primary), others.collect(Other))


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

private class LanguageTagImpl(val primary: String, val others: Seq[String]) extends LanguageTag

