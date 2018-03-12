package scamper.types

import scamper.ListParser

/**
 * Standardized type for Link header value.
 *
 * @see [[scamper.headers.Link]]
 */
trait LinkValue {
  /** Link reference */
  def ref: String

  /** Link parameters */
  def params: Map[String, Option[String]]

  /** Returns formatted link. */
  override lazy val toString: String =
    '<' + ref + '>' + LinkParams.format(params)
}

/** LinkValue factory */
object LinkValue {
  private val syntax = """\s*<([^,<>"]+)>\s*(;.+)?\s*""".r

  /** Creates LinkValue with supplied values. */
  def apply(ref: String, params: (String, Option[String])*): LinkValue =
    LinkValueImpl(ref, params.toMap)

  /** Creates LinkValue with supplied values. */
  def apply(ref: String, params: Map[String, Option[String]]): LinkValue =
    LinkValueImpl(ref, params)

  /** Destructures LinkValue. */
  def unapply(link: LinkValue): Option[(String, Map[String, Option[String]])] =
    Some((link.ref, link.params))

  /** Parses formatted link. */
  def parse(link: String): LinkValue =
    link match {
      case syntax(ref, null)   => apply(ref)
      case syntax(ref, params) => apply(ref, LinkParams.parse(params))
      case _ => throw new IllegalArgumentException(s"Malformed link: $link")
    }

  /** Parses formatted list of links. */
  def parseAll(links: String): Seq[LinkValue] =
    ListParser(links).map(parse)
}

private case class LinkValueImpl(ref: String, params: Map[String, Option[String]]) extends LinkValue

