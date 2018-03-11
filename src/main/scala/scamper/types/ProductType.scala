package scamper.types

import scamper.Grammar.Token

/**
 * Standardized type for User-Agent and Server header value.
 *
 * @see [[scamper.headers.UserAgent]]
 * @see [[scamper.headers.Server]]
 */
trait ProductType {
  /** Product name */
  def name: String

  /** Product version */
  def version: Option[String]

  /** Returns formatted product. */
  override lazy val toString: String =
    name + version.map('/' + _).getOrElse("")
}

/** ProductType factory */
object ProductType {
  private val syntax = """([\w!#$%&'*+.^`|~-]+)(?:/([\w!#$%&'*+.^`|~-]+))?(?:\s+\(.*\)\s*)?""".r

  /** Parses formatted list of products. */
  def parseAll(products: String): Seq[ProductType] =
    syntax.findAllIn(products).map(apply).toSeq

  /** Parses formatted product. */
  def apply(product: String): ProductType =
    product match {
      case syntax(name, version) => new ProductTypeImpl(name, Option(version))
      case _ => throw new IllegalArgumentException(s"Malformed product: $product")
    }

  /** Creates ProductType with supplied values. */
  def apply(name: String, version: Option[String]): ProductType =
    new ProductTypeImpl(CheckToken(name), version.map(CheckToken))

  /** Destructures ProductType. */
  def unapply(product: ProductType): Option[(String, Option[String])] =
    Some((product.name, product.version))

  private def CheckToken(token: String): String =
    Token(token).getOrElse {
      throw new IllegalArgumentException(s"Invalid token: $token")
    }
}

private case class ProductTypeImpl(name: String, version: Option[String]) extends ProductType

