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
 * Standardized type for Cache-Control header value.
 *
 * @see [[scamper.http.headers.CacheControl]]
 */
trait CacheDirective:
  /** Gets directive name. */
  def name: String

  /** Gets optional directive value. */
  def value: Option[String]

  /** Returns formatted directive. */
  override lazy val toString: String =
    name + value.map(x => "=" + Token(x).getOrElse(s"""\"$x\"""")).getOrElse("")

/** Provides factory for `CacheDirective`. */
object CacheDirective:
  import CacheDirectives.*

  private val syntax1 = """\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val syntax2 = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val syntax3 = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r

  /** Parses formatted directive. */
  def parse(directive: String): CacheDirective =
    directive match
      case syntax1(name) => apply(name)
      case syntax2(name, value) => apply(name, Some(value))
      case syntax3(name, value) => apply(name, Some(value))
      case _ => throw IllegalArgumentException(s"Malformed cache directive: $directive")

  /** Parses formatted list of directives. */
  def parseAll(directives: String): Seq[CacheDirective] =
    ListParser(directives).map(parse)

  /** Creates directive with supplied name and value. */
  def apply(name: String, value: Option[String] = None): CacheDirective =
    Token(name.toLowerCase) map {
      case "immutable"              => `immutable`
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
      throw IllegalArgumentException(s"Invalid cache directive name: $name")
    }

private case class CacheDirectiveImpl(name: String, value: Option[String]) extends CacheDirective
