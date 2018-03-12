package scamper.types

import scamper.Grammar.Token
import scamper.ListParser

/**
 * Standardized type for Cache-Control header value.
 *
 * @see [[scamper.headers.CacheControl]]
 */
trait CacheDirective {
  /** Directive name */
  def name: String

  /** Directive optional value */
  def value: Option[String]

  /** Returns formatted cache directive. */
  override lazy val toString: String =
    name + value.map(x => '=' + Token(x).getOrElse('"' + x + '"')).getOrElse("")
}

import CacheDirectives._

/** CacheDirective factory */
object CacheDirective {
  private val syntax1 = """\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val syntax2 = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val syntax3 = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r

  /** Creates CacheDirective with supplied name and value. */
  def apply(name: String, value: Option[String] = None): CacheDirective =
    Token(name.toLowerCase) map {
      case "max-age"                => `max-age`(value.getOrElse("0").toLong)
      case "max-stale"              => `max-stale`(value.getOrElse("0").toLong)
      case "min-fresh"              => `min-fresh`(value.getOrElse("0").toLong)
      case "must-revalidate"        => `must-revalidate`
      case "no-cache"               => `no-cache`
      case "no-store"               => `no-store`
      case "no-transform"           => `no-transform`
      case "only-if-cached"         => `only-if-cached`
      case "private"                => `private`
      case "proxy-revalidate"       => `proxy-revalidate`
      case "public"                 => `public`
      case "s-maxage"               => `s-maxage`(value.getOrElse("0").toLong)
      case "stale-if-error"         => `stale-if-error`(value.getOrElse("0").toLong)
      case "stale-while-revalidate" => `stale-while-revalidate`(value.getOrElse("0").toLong)
      case token                    => CacheDirectiveImpl(token, value)
    } getOrElse {
      throw new IllegalArgumentException(s"Invalid cache directive name: $name")
    }

  /** Destructures CacheDirective. */
  def unapply(directive: CacheDirective): Option[(String, Option[String])] =
    Some(directive.name -> directive.value)

  /** Parse formatted cache directive. */
  def parse(directive: String): CacheDirective =
    directive match {
      case syntax1(name) => apply(name)
      case syntax2(name, value) => apply(name, Some(value))
      case syntax3(name, value) => apply(name, Some(value))
      case _ => throw new IllegalArgumentException(s"Malformed cache directive: $directive")
    }

  /** Parse formatted list of cache directives. */
  def parseAll(directives: String): Seq[CacheDirective] =
    ListParser(directives).map(parse)
}

private case class CacheDirectiveImpl(val name: String, val value: Option[String]) extends CacheDirective

