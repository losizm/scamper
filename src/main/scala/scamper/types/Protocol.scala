package scamper.types

import scamper.Grammar.Token

/**
 * Standardized type for Upgrade header value.
 *
 * @see [[scamper.ImplicitHeaders.Upgrade]]
 */
trait Protocol {
  /** Protocol name */
  def name: String

  /** Protocol version */
  def version: Option[String]

  /** Returns formatted protocol. */
  override lazy val toString: String =
    name + version.map('/' + _).getOrElse("")
}

/** Protocol factory */
object Protocol {
  private val syntax = """\s*([\w!#$%&'*+.^`|~-]+)(?:/([\w!#$%&'*+.^`|~-]+))?\s*""".r

  /** Parses formatted protocol. */
  def apply(protocol: String): Protocol =
    protocol match {
      case syntax(name, version) => ProtocolImpl(name, Option(version))
      case _ => throw new IllegalArgumentException(s"Malformed protocol: $protocol")
    }

  /** Creates Protocol with supplied values. */
  def apply(name: String, version: Option[String]): Protocol =
    ProtocolImpl(CheckToken(name), version.map(CheckToken))

  /** Destructures Protocol. */
  def unapply(protocol: Protocol): Option[(String, Option[String])] =
    Some((protocol.name, protocol.version))

  private def CheckToken(token: String): String =
    Token(token).getOrElse {
      throw new IllegalArgumentException(s"Invalid token: $token")
    }
}

private case class ProtocolImpl(name: String, version: Option[String]) extends Protocol

