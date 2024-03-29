/*
 * Copyright 2021 Carlos Conyers
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
package scamper
package http
package types

/**
 * Standardized type for Via header value.
 *
 * @see [[scamper.http.headers.Via]]
 */
trait ViaType:
  /** Gets received protcol. */
  def protocol: Protocol

  /** Gets received by. */
  def by: String

  /** Gets comment. */
  def comment: Option[String]

  /** Returns formatted via. */
  override lazy val toString: String =
    (if protocol.name == "HTTP" then protocol.version.getOrElse("-") else protocol.toString) + ' ' + by + comment.map(" (" + _ + ')').getOrElse("")

/** Provides factory for `ViaType`. */
object ViaType:
  private val syntax = """\s*(?:([\w!#$%&'*+.^`|~-]+)/)?([\w!#$%&'*+.^`|~-]+)\s+([\w!#$%&'*+.:^`|~-]+)(?:\s+\(\s*(.*?)\s*\))?\s*""".r

  /** Parses formatted via. */
  def parse(via: String): ViaType =
    via match
      case syntax(null, version, by, comment)   => ViaTypeImpl(Protocol("HTTP", Some(version)), by, Option(comment))
      case syntax(name, version, by, comment)   => ViaTypeImpl(Protocol(name, Some(version)), by, Option(comment))
      case _ => throw IllegalArgumentException(s"Malformed via: $via")

  /** Parses formatted list of vias. */
  def parseAll(vias: String): Seq[ViaType] =
    ListParser(vias).map(parse)

  /** Creates via type with supplied values. */
  def apply(protocol: Protocol, by: String, comment: Option[String] = None): ViaType =
    ViaTypeImpl(protocol, by, comment.map(_.trim))

private case class ViaTypeImpl(protocol: Protocol, by: String, comment: Option[String]) extends ViaType
