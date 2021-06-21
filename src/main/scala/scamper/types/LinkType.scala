/*
 * Copyright 2017-2020 Carlos Conyers
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
trait LinkType {
  /** Gets link reference. */
  def ref: Uri

  /** Gets link parameters. */
  def params: Map[String, Option[String]]

  /** Returns formatted link. */
  override lazy val toString: String =
    s"<$ref>${HeaderParams.format(params)}"
}

/** Provides factory for `LinkType`. */
object LinkType {
  private val syntax = """\s*<([^,<>"]+)>\s*(;.+)?\s*""".r

  /** Parses formatted link. */
  def parse(link: String): LinkType =
    link match {
      case syntax(ref, null)   => apply(Uri(ref))
      case syntax(ref, params) => apply(Uri(ref), HeaderParams.parse(params))
      case _ => throw new IllegalArgumentException(s"Malformed link: $link")
    }

  /** Parses formatted list of links. */
  def parseAll(links: String): Seq[LinkType] =
    ListParser(links).map(parse)

  /** Creates link with supplied values. */
  def apply(ref: Uri, params: (String, Option[String])*): LinkType =
    LinkTypeImpl(ref, params.toMap)

  /** Creates link with supplied values. */
  def apply(ref: Uri, params: Map[String, Option[String]]): LinkType =
    LinkTypeImpl(ref, params)
}

private case class LinkTypeImpl(ref: Uri, params: Map[String, Option[String]]) extends LinkType
