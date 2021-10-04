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
package websocket

import Grammar.Token

/**
 * Standardized type for Sec-WebSocket-Extensions header value.
 *
 * @see [[SecWebSocketExtensions]]
 */
trait WebSocketExtension:
  /** Gets extension identifier. */
  def identifier: String

  /** Gets extension parameters. */
  def params: Map[String, Option[String]]

/** Provides factory for `WebSocketExtension`. */
object WebSocketExtension:
  private val syntax = """\s*([^,;\s]+)\s*(;.+)?\s*""".r

  /** Creates WebSocket extension from supplied values. */
  def apply(identifier: String, params: Map[String, Option[String]]): WebSocketExtension =
    WebSocketExtensionImpl(checkIdentifier(identifier), checkParams(params))

  /** Creates WebSocket extension from supplied values. */
  def apply(identifier: String, params: (String, Option[String])*): WebSocketExtension =
    apply(identifier, params.toMap)

  /** Parses formatted extension. */
  def parse(extension: String): WebSocketExtension =
    extension match
      case syntax(identifier, null)   => apply(identifier)
      case syntax(identifier, params) => apply(identifier, HeaderParams.parse(params))

  /** Parses formatted list of extensions. */
  def parseAll(extensions: String): Seq[WebSocketExtension] =
    ListParser(extensions).map(parse)

  private def checkIdentifier(identifier: String): String =
    Token(identifier)
      .getOrElse(throw IllegalArgumentException(s"Invalid extension identifier: $identifier"))

  private def checkParams(params: Map[String, Option[String]]): Map[String, Option[String]] =
    params.keys.foreach { name =>
      if Token(name).isEmpty then
        throw IllegalArgumentException(s"Invalid extension parameter: $name")
    }
    params

private case class WebSocketExtensionImpl(identifier: String, params: Map[String, Option[String]]) extends WebSocketExtension:
  override lazy val toString: String =
    s"${identifier}${HeaderParams.format(params)}"
