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

import scamper.{ HeaderParams, ListParser, Uri }

/**
 * Standardized type for Link header value.
 *
 * @see [[scamper.headers.Link]]
 */
trait LinkValue {
  /** Gets link reference. */
  def ref: Uri

  /** Gets link parameters. */
  def params: Map[String, Option[String]]

  /** Returns formatted link. */
  override lazy val toString: String =
    s"<$ref>${HeaderParams.format(params)}"
}

/** Provides factory methods for `LinkValue`. */
object LinkValue {
  private val syntax = """\s*<([^,<>"]+)>\s*(;.+)?\s*""".r

  /** Parses formatted link. */
  def parse(link: String): LinkValue =
    link match {
      case syntax(ref, null)   => apply(Uri(ref))
      case syntax(ref, params) => apply(Uri(ref), HeaderParams.parse(params))
      case _ => throw new IllegalArgumentException(s"Malformed link: $link")
    }

  /** Parses formatted list of links. */
  def parseAll(links: String): Seq[LinkValue] =
    ListParser(links).map(parse)

  /** Creates LinkValue with supplied values. */
  def apply(ref: Uri, params: (String, Option[String])*): LinkValue =
    LinkValueImpl(ref, params.toMap)

  /** Creates LinkValue with supplied values. */
  def apply(ref: Uri, params: Map[String, Option[String]]): LinkValue =
    LinkValueImpl(ref, params)

  /** Destructures LinkValue. */
  def unapply(link: LinkValue): Option[(Uri, Map[String, Option[String]])] =
    Some((link.ref, link.params))
}

private case class LinkValueImpl(ref: Uri, params: Map[String, Option[String]]) extends LinkValue
