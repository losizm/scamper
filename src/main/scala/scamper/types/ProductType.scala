/*
 * Copyright 2019 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper.types

import scamper.Grammar.Token

/**
 * Standardized type for User-Agent and Server header value.
 *
 * @see [[scamper.headers.UserAgent]]
 * @see [[scamper.headers.Server]]
 */
trait ProductType {
  /** Gets product name. */
  def name: String

  /** Gets product version. */
  def version: Option[String]

  /** Returns formatted product. */
  override lazy val toString: String =
    name + version.map("/" + _).getOrElse("")
}

/** Provides factory methods for `ProductType`. */
object ProductType {
  private val syntax = """([\w!#$%&'*+.^`|~-]+)(?:/([\w!#$%&'*+.^`|~-]+))?(?:\s+\(.*\)\s*)?""".r

  /** Parses formatted product. */
  def parse(product: String): ProductType =
    product match {
      case syntax(name, version) => ProductTypeImpl(name, Option(version))
      case _ => throw new IllegalArgumentException(s"Malformed product: $product")
    }

  /** Parses formatted list of products. */
  def parseAll(products: String): Seq[ProductType] =
    syntax.findAllIn(products).map(parse).toSeq

  /** Creates ProductType with supplied values. */
  def apply(name: String, version: Option[String]): ProductType =
    ProductTypeImpl(CheckToken(name), version.map(CheckToken))

  /** Destructures ProductType. */
  def unapply(product: ProductType): Option[(String, Option[String])] =
    Some((product.name, product.version))

  private def CheckToken(token: String): String =
    Token(token).getOrElse {
      throw new IllegalArgumentException(s"Invalid token: $token")
    }
}

private case class ProductTypeImpl(name: String, version: Option[String]) extends ProductType
