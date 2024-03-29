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

import Grammar.Token

/**
 * Standardized type for Upgrade header value.
 *
 * @see [[scamper.http.headers.Upgrade]]
 */
trait Protocol:
  /** Gets protocol name. */
  def name: String

  /** Gets protocol version. */
  def version: Option[String]

  /** Returns formatted protocol. */
  override lazy val toString: String =
    name + version.map("/" + _).getOrElse("")

/** Provides factory for `Protocol`. */
object Protocol:
  private val syntax = """\s*([\w!#$%&'*+.^`|~-]+)(?:/([\w!#$%&'*+.^`|~-]+))?\s*""".r

  /** Parses formatted protocol. */
  def parse(protocol: String): Protocol =
    protocol match
      case syntax(name, version) => ProtocolImpl(name, Option(version))
      case _ => throw IllegalArgumentException(s"Malformed protocol: $protocol")

  /** Creates protocol with supplied values. */
  def apply(name: String, version: Option[String]): Protocol =
    ProtocolImpl(CheckToken(name), version.map(CheckToken))

  private def CheckToken(token: String): String =
    Token(token).getOrElse {
      throw IllegalArgumentException(s"Invalid token: $token")
    }

private case class ProtocolImpl(name: String, version: Option[String]) extends Protocol
