package scamper

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => dateFormatter }

import CookieHelper._

/**
 * HTTP Cookie
 *
 * @see [[PlainCookie]], [[SetCookie]]
 */
sealed trait Cookie {
  /** Cookie name */
  def name: String

  /** Cookie value */
  def value: String
}

/** HTTP Plain Cookie */
case class PlainCookie private (name: String, value: String) extends Cookie {
  /** Converts to SetCookie using name-value pair. */
  def toSetCookie: SetCookie = SetCookie(name, value)

  /** Converts to SetCookie using name-value pair and supplied attributes. */
  def toSetCookie(path: Option[String] = None, domain: Option[String] = None, maxAge: Option[Long] = None,
      expires: Option[OffsetDateTime] = None, secure: Boolean = false, httpOnly: Boolean = false): SetCookie =
    SetCookie(name, value, path, domain, maxAge, expires, secure, httpOnly)

  /** Returns formatted cookie. */
  override lazy val toString: String = s"$name=$value"
}

/** PlainCookie factory */
object PlainCookie {
  /** Creates PlainCookie using supplied name and value. */
  def apply(name: String, value: String): PlainCookie =
    new PlainCookie(Name(name), Value(value))

  /** Parses formatted cookie. */
  def apply(cookie: String): PlainCookie =
    cookie.split("=", 2) match {
      case Array(name, value) => apply(name.trim, value.trim)
      case _ => throw new IllegalArgumentException(s"Malformed cookie: $cookie")
    }
}

/** HTTP Set-Cookie */
case class SetCookie private (name: String, value: String, path: Option[String], domain: Option[String], maxAge: Option[Long],
    expires: Option[OffsetDateTime], secure: Boolean, httpOnly: Boolean) extends Cookie {
  /** Converts to PlainCookie using name-value pair. */
  def toPlainCookie: PlainCookie = PlainCookie(name, value)

  /** Returns formatted cookie. */
  override lazy val toString: String = {
    val cookie = new StringBuilder

    cookie.append(name).append('=').append(value)

    path.foreach(cookie.append("; Path=").append(_))
    domain.foreach(cookie.append("; Domain=").append(_))
    maxAge.foreach(cookie.append("; Max-Age=").append(_))
    expires.foreach(date => cookie.append("; Expires=").append(dateFormatter.format(date)))

    if (secure) cookie.append("; Secure")
    if (httpOnly) cookie.append("; HttpOnly")

    cookie.toString
  }
}

/** SetCookie factory */
object SetCookie {
  /** Creates SetCookie using supplied name, value, and attributes. */
  def apply(name: String, value: String, path: Option[String] = None, domain: Option[String] = None, maxAge: Option[Long] = None,
      expires: Option[OffsetDateTime] = None, secure: Boolean = false, httpOnly: Boolean = false): SetCookie =
    new SetCookie(Name(name), Value(value), path, domain, maxAge, expires, secure, httpOnly)

  /** Parses formatted cookie. */
  def apply(cookie: String): SetCookie =
    cookie.split(";", 2) match {
      case Array(pair, attribs) => apply(PlainCookie(pair), CookieAttributes(attribs))
      case Array(pair)          => apply(PlainCookie(pair), CookieAttributes())
    }

  private def apply(cookie: PlainCookie, attribs: CookieAttributes): SetCookie =
    apply(cookie.name, cookie.value, attribs.path, attribs.domain, attribs.maxAge, attribs.expires, attribs.secure, attribs.httpOnly)
}

