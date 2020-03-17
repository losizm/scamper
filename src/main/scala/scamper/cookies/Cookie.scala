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
package scamper.cookies

import java.time.Instant

import scamper.DateValue

import CookieGrammar._

/** HTTP Cookie */
sealed trait Cookie {
  /** Gets cookie name. */
  def name: String

  /** Gets cookie value. */
  def value: String
}

/**
 * HTTP request cookie
 *
 * @see [[SetCookie]]
 */
trait PlainCookie extends Cookie {
  /** Returns formatted cookie. */
  override lazy val toString: String = s"$name=$value"
}

/** Provides factory methods for `PlainCookie`. */
object PlainCookie {
  /** Parses formatted cookie. */
  def parse(cookie: String): PlainCookie =
    cookie.split("=", 2) match {
      case Array(name, value) => apply(name.trim, value.trim)
      case _ => throw new IllegalArgumentException(s"Malformed cookie: $cookie")
    }

  /** Creates PlainCookie with supplied name-value pair. */
  def apply(name: String, value: String): PlainCookie =
    PlainCookieImpl(Name(name), Value(value))

  /** Destructures PlainCookie. */
  def unapply(cookie: PlainCookie): Option[(String, String)] =
    Some((cookie.name, cookie.value))
}

private case class PlainCookieImpl(name: String, value: String) extends PlainCookie

/**
 * HTTP response cookie
 *
 * @see [[PlainCookie]]
 */
trait SetCookie extends Cookie {
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

  /** Tests whether cookie should be limited to secure channels. */
  def secure: Boolean

  /** Tests whether cookie should be limited to HTTP requests. */
  def httpOnly: Boolean

  /** Converts to PlainCookie using name-value pair. */
  def toPlainCookie: PlainCookie = PlainCookie(name, value)

  /** Returns formatted cookie. */
  override lazy val toString: String = {
    val cookie = new StringBuilder

    cookie.append(name).append('=').append(value)

    domain.foreach(cookie.append("; Domain=").append(_))
    path.foreach(cookie.append("; Path=").append(_))
    expires.foreach(date => cookie.append("; Expires=").append(DateValue.format(date)))
    maxAge.foreach(cookie.append("; Max-Age=").append(_))

    if (secure) cookie.append("; Secure")
    if (httpOnly) cookie.append("; HttpOnly")

    cookie.toString
  }
}

/** Provides factory methods for `SetCookie`. */
object SetCookie {
  /** Parses formatted cookie. */
  def parse(cookie: String): SetCookie =
    cookie.split(";", 2) match {
      case Array(pair, attrs) =>
        pair.split("=") match {
          case Array(name, value) => SetCookieImpl(Name(name), Value(value), CookieAttributes.parse(attrs))
        }
      case Array(pair) =>
        pair.split("=") match {
          case Array(name, value) => SetCookieImpl(Name(name), Value(value), CookieAttributes())
        }
    }

  /** Creates SetCookie with supplied name, value, and attributes. */
  def apply(name: String, value: String, domain: Option[String] = None, path: Option[String] = None, expires: Option[Instant] = None,
      maxAge: Option[Long] = None, secure: Boolean = false, httpOnly: Boolean = false): SetCookie =
    SetCookieImpl(Name(name), Value(value), CookieAttributes(domain, path, expires, maxAge, secure, httpOnly))

  /** Destructures SetCookie. */
  def unapply(cookie: SetCookie): Option[(String, String, Option[String], Option[String], Option[Instant], Option[Long], Boolean, Boolean)] =
    Some((cookie.name, cookie.value, cookie.domain, cookie.path, cookie.expires, cookie.maxAge, cookie.secure, cookie.httpOnly))
}

private case class SetCookieImpl(name: String, value: String, attrs: CookieAttributes) extends SetCookie {
  def domain: Option[String] = attrs.domain
  def path: Option[String] = attrs.path
  def expires: Option[Instant] = attrs.expires
  def maxAge: Option[Long] = attrs.maxAge
  def secure: Boolean = attrs.secure
  def httpOnly: Boolean = attrs.httpOnly
}
