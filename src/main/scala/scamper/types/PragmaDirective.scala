package scamper.types

import scamper.Grammar.Token
import scamper.ListParser

/**
 * Standardized type for Pragma header value.
 *
 * @see [[scamper.ImplicitHeaders.Pragma]]
 */
trait PragmaDirective {
  /** Directive name */
  def name: String

  /** Optinal directive value */
  def value: Option[String]

  /** Returns formatted pragma directive. */
  override lazy val toString: String =
    name + value.map(x => '=' + Token(x).getOrElse('"' + x + '"')).getOrElse("")
}

/** PragmaDirective factory */
object PragmaDirective {
  import PragmaDirectives._

  private val syntax1 = """\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val syntax2 = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val syntax3 = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r

  /** Creates PragmaDirective with supplied name and value. */
  def apply(name: String, value: Option[String] = None): PragmaDirective =
    Token(name.toLowerCase).map {
      case "no-cache" => `no-cache`
      case token      => PragmaDirectiveImpl(token, value)
    } getOrElse {
      throw new IllegalArgumentException(s"Invalid pragma directive name: $name")
    }

  /** Destructures PragmaDirective. */
  def unapply(directive: PragmaDirective): Option[(String, Option[String])] =
    Some(directive.name -> directive.value)

  /** Parse formatted pragma directive. */
  def parse(directive: String): PragmaDirective =
    directive match {
      case syntax1(name) => apply(name)
      case syntax2(name, value) => apply(name, Some(value))
      case syntax3(name, value) => apply(name, Some(value))
      case _ => throw new IllegalArgumentException(s"Malformed pragma directive: $directive")
    }

  /** Parse formatted list of pragma directives. */
  def parseAll(directives: String): Seq[PragmaDirective] =
    ListParser(directives).map(parse)
}

private case class PragmaDirectiveImpl(name: String, value: Option[String]) extends PragmaDirective

