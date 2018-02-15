package scamper

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => dateFormatter }

import CookieHelper._

/** HTTP Set-Cookie */
case class SetCookie private (name: String, value: String, path: Option[String], domain: Option[String], maxAge: Option[Long],
    expires: Option[OffsetDateTime], secure: Boolean, httpOnly: Boolean) {
  /** Converts to Cookie using name-value pair. */
  lazy val toCookie: Cookie = Cookie(name, value)

  /** Returns formatted cookie. */
  override def toString(): String = {
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
      case Array(pair, attribs) => apply(Cookie(pair), CookieAttributes(attribs))
      case Array(pair)          => apply(Cookie(pair), CookieAttributes())
    }

  private def apply(cookie: Cookie, attribs: CookieAttributes): SetCookie =
    apply(cookie.name, cookie.value, attribs.path, attribs.domain, attribs.maxAge, attribs.expires, attribs.secure, attribs.httpOnly)
}

