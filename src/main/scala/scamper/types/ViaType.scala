package scamper.types

import scamper.ListParser

/**
 * Standardized type for Via header value.
 *
 * @see [[scamper.ImplicitHeaders.Via]]
 */
trait ViaType {
  /** Received protcol */
  def protocol: Protocol

  /** Received by */
  def by: String

  /** Comment */
  def comment: Option[String]

  /** Returns formatted via. */
  override lazy val toString: String =
    (if (protocol.name == "HTTP") protocol.version.getOrElse("-") else protocol.toString) + ' ' + by + comment.map(" (" + _ + ')').getOrElse("")
}

/** ViaType factory */
object ViaType {
  private val syntax = """\s*(?:([\w!#$%&'*+.^`|~-]+)/)?([\w!#$%&'*+.^`|~-]+)\s+([\w!#$%&'*+.:^`|~-]+)(?:\s+\(\s*(.*?)\s*\))?\s*""".r

  /** Creates ViaType with supplied values. */
  def apply(protocol: Protocol, by: String, comment: Option[String] = None): ViaType =
    ViaTypeImpl(protocol, by, comment.map(_.trim))

  /** Destructures ViaType. */
  def unapply(via: ViaType): Option[(Protocol, String, Option[String])] =
    Some((via.protocol, via.by, via.comment))

  /** Parses formatted via. */
  def parse(via: String): ViaType =
    via match {
      case syntax(null, version, by, comment)   => new ViaTypeImpl(Protocol("HTTP", Some(version)), by, Option(comment))
      case syntax(name, version, by, comment)   => new ViaTypeImpl(Protocol(name, Some(version)), by, Option(comment))
      case _ => throw new IllegalArgumentException(s"Malformed via: $via")
    }

  /** Parses formatted list of vias. */
  def parseAll(vias: String): Seq[ViaType] =
    ListParser(vias).map(parse)

  private def checkBy(by: String): String =
    if (by.matches("""[\w!#$%&'*+.:^`|~-]+""")) by
    else throw new IllegalArgumentException(s"Invalid received-by: $by")
}

private case class ViaTypeImpl(protocol: Protocol, by: String, comment: Option[String]) extends ViaType

