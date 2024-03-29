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
package cookies

import java.time.Instant

import CookieGrammar.*

/** Defines HTTP cookie. */
sealed trait Cookie:
  /** Gets cookie name. */
  def name: String

  /** Gets cookie value. */
  def value: String

/**
 * Defines HTTP request cookie.
 *
 * @see [[SetCookie]]
 */
trait PlainCookie extends Cookie

/** Provides factory for `PlainCookie`. */
object PlainCookie:
  /** Parses formatted cookie. */
  def parse(cookie: String): PlainCookie =
    cookie.split("=", 2) match
      case Array(name, value) => apply(name.trim, value.trim)
      case _ => throw IllegalArgumentException(s"Malformed cookie: $cookie")

  /** Parses formatted list of cookies. */
  def parseAll(cookies: String): Seq[PlainCookie] =
    ListParser(cookies, semicolon = true)
      .map(PlainCookie.parse)

  /** Creates cookie with supplied name and value. */
  def apply(name: String, value: String): PlainCookie =
    PlainCookieImpl(Name(name), Value(value))

private case class PlainCookieImpl(name: String, value: String) extends PlainCookie:
  override lazy val toString: String = s"$name=$value"

/**
 * Defines HTTP response cookie.
 *
 * @see [[PlainCookie]]
 */
trait SetCookie extends Cookie:
  /** Gets cookie domain. */
  def domain: Option[String]

  /** Gets cookie path. */
  def path: Option[String]

  /** Gets maximum liftetime of cookie represented as time of expiry. */
  def expires: Option[Instant]

  /**
   * Gets maximum liftetime of cookie represented as number of seconds until
   * expiry.
   */
  def maxAge: Option[Long]

  /** Tests for secure cookie. */
  def secure: Boolean

  /** Tests HTTP only cookie. */
  def httpOnly: Boolean

  /** Converts to `PlainCookie`. */
  def toPlainCookie: PlainCookie = PlainCookie(name, value)

/** Provides factory for `SetCookie`. */
object SetCookie:
  /** Parses formatted cookie. */
  def parse(cookie: String): SetCookie =
    cookie.split(";", 2) match
      case Array(pair, attrs) =>
        pair.split("=", 2) match
          case Array(name, value) => SetCookieImpl(Name(name), Value(value), CookieAttributes.parse(attrs))
      case Array(pair) =>
        pair.split("=", 2) match
          case Array(name, value) => SetCookieImpl(Name(name), Value(value), CookieAttributes())

  /** Creates cookie with supplied name, value, and attributes. */
  def apply(name: String, value: String, domain: Option[String] = None, path: Option[String] = None, expires: Option[Instant] = None,
      maxAge: Option[Long] = None, secure: Boolean = false, httpOnly: Boolean = false): SetCookie =
    SetCookieImpl(Name(name), Value(value), CookieAttributes(domain, path, expires, maxAge, secure, httpOnly))

private case class SetCookieImpl(name: String, value: String, attrs: CookieAttributes) extends SetCookie:
  def domain: Option[String] = attrs.domain
  def path: Option[String] = attrs.path
  def expires: Option[Instant] = attrs.expires
  def maxAge: Option[Long] = attrs.maxAge
  def secure: Boolean = attrs.secure
  def httpOnly: Boolean = attrs.httpOnly

  override lazy val toString: String =
    val cookie = StringBuilder()

    cookie.append(name).append('=').append(value)

    domain.foreach(cookie.append("; Domain=").append(_))
    path.foreach(cookie.append("; Path=").append(_))
    expires.foreach(date => cookie.append("; Expires=").append(DateValue.format(date)))
    maxAge.foreach(cookie.append("; Max-Age=").append(_))

    if secure then cookie.append("; Secure")
    if httpOnly then cookie.append("; HttpOnly")

    cookie.toString

/** Persistent cookie in [[CookieStore]]. */
sealed trait PersistentCookie extends Cookie:
  /** Gets cookie domain. */
  def domain: String

  /** Gets cookie path. */
  def path: String

  /** Indicates whether cookie should be limited to secure channels. */
  def secureOnly: Boolean

  /** Indicates whether cookie should be limited to HTTP requests. */
  def httpOnly: Boolean

  /**
   * Indicates whether cookie should be limited to request host.
   *
   * If `true`, the request host must be identical to cookie domain; otherwise,
   * if `false`, the request host must simply "match" cookie domain.
   */
  def hostOnly: Boolean

  /** Indicates whether cookie should be persistent after current session. */
  def persistent: Boolean

  /** Gets cookie's creation time. */
  def creation: Instant

  /** Gets cookie's last access time. */
  def lastAccess: Instant

  /** Gets cookie's expiry time. */
  def expiry: Instant

  /**
   * Updates last access time and returns cookie.
   *
   * @return this cookie
   */
  def touch(): this.type

  /** Converts to `PlainCookie`. */
  def toPlainCookie: PlainCookie = PlainCookie(name, value)

private case class PersistentCookieImpl(
  name: String,
  value: String,
  domain: String = "",
  path: String = "/",
  secureOnly: Boolean = false,
  httpOnly: Boolean = false,
  hostOnly: Boolean = false,
  persistent: Boolean = false,
  creation: Instant = Instant.now(),
  expiry: Instant = Instant.now()) extends PersistentCookie:

  private var _lastAccess: Instant = Instant.now()
  def lastAccess = _lastAccess
  def touch() =
    _lastAccess = Instant.now()
    this
