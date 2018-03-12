package scamper.types

import java.time.OffsetDateTime

import scamper.{ DateValue, ListParser }

/**
 * Standardized type for Warning header value.
 *
 * @see [[scamper.headers.Warning]]
 */
trait WarningType {
  /** Warning code */
  def code: Int

  /** Warning agent */
  def agent: String

  /** Warning text */
  def text: String

  /** Warning date */
  def date: Option[OffsetDateTime]

  /** Returns formatted warning. */
  override lazy val toString: String =
    code + " " + agent + " \"" + text + '"' + date.map(x => " \"" + DateValue.format(x) + '"').getOrElse("")
}

/** WarningType factory */
object WarningType {
  private val syntax = """\s*(\d{3})\s*([\p{Graph}&&[^",]]+)\s*"([^"]*)"\s*(?:"([\w, :+-]+)")?\s*""".r

  /** Creates WarningType with supplied values. */
  def apply(code: Int, agent: String, text: String, date: Option[OffsetDateTime] = None): WarningType =
    WarningTypeImpl(code, agent, text, date)

  /** Destructures WarningType. */
  def unapply(warning: WarningType): Option[(Int, String, String, Option[OffsetDateTime])] =
    Some((warning.code, warning.agent, warning.text, warning.date))

  /** Parses formatted warning. */
  def parse(warning: String): WarningType =
    warning match {
      case syntax(code, agent, text, null) => apply(code.toInt, agent, text)
      case syntax(code, agent, text, date) => apply(code.toInt, agent, text, Some(DateValue.parse(date)))
      case _ => throw new IllegalArgumentException(s"Malformed warning: $warning")
    }

  /** Parses formatted list of warnings. */
  def parseAll(warnings: String): Seq[WarningType] =
    ListParser(warnings).map(parse)
}

private case class WarningTypeImpl(code: Int, agent: String, text: String, date: Option[OffsetDateTime]) extends WarningType

