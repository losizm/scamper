package scamper

import java.time.OffsetDateTime

import CookieHelper._

/** HTTP Cookie */
case class Cookie private (name: String, value: String) {
  /** Returns formatted cookie. */
  override val toString: String = s"$name=$value"

  /** Converts to SetCookie using name-value pair. */
  def toSetCookie: SetCookie = SetCookie(name, value)

  /** Converts to SetCookie using name-value pair and supplied attributes. */
  def toSetCookie(path: Option[String] = None, domain: Option[String] = None, maxAge: Option[Long] = None,
      expires: Option[OffsetDateTime] = None, secure: Boolean = false, httpOnly: Boolean = false): SetCookie =
    SetCookie(name, value, path, domain, maxAge, expires, secure, httpOnly)
}

/** Cookie factory */
object Cookie {
  /** Creates Cookie using supplied name and value. */
  def apply(name: String, value: String): Cookie =
    new Cookie(Name(name), Value(value))

  /** Parses formatted cookie. */
  def apply(cookie: String): Cookie =
    cookie.split("=", 2) match {
      case Array(name, value) => apply(name.trim, value.trim)
      case _ => throw new IllegalArgumentException(s"Malformed cookie: $cookie")
    }
}

