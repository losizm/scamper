package scamper

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => dateFormatter }

import scala.util.Try

private case class CookieAttributes(path: Option[String] = None, domain: Option[String] = None, maxAge: Option[Long] = None,
    expires: Option[OffsetDateTime] = None, secure: Boolean = false, httpOnly: Boolean = false)

private object CookieAttributes {
  def apply(attribs: String): CookieAttributes =
    attribs.split(";").map(_.split("=", 2)).foldRight(CookieAttributes())(append)

  private def append(attrib: Array[String], attribs: CookieAttributes): CookieAttributes =
    attrib match {
      case Array(name, value) if name.trim.equalsIgnoreCase("Path")     => attribs.copy(path = Some(value))
      case Array(name, value) if name.trim.equalsIgnoreCase("Domain")   => attribs.copy(domain = Some(value))
      case Array(name, value) if name.trim.equalsIgnoreCase("Max-Age")  => attribs.copy(maxAge = toMaxAge(value))
      case Array(name, value) if name.trim.equalsIgnoreCase("Expires")  => attribs.copy(expires = toExpires(value))
      case Array(name)        if name.trim.equalsIgnoreCase("Secure")   => attribs.copy(secure = true)
      case Array(name)        if name.trim.equalsIgnoreCase("HttpOnly") => attribs.copy(httpOnly = true)
      case _ => attribs
    }

  private def toMaxAge(value: String): Option[Long] =
    Try(value.trim.toLong).toOption

  private def toExpires(value: String): Option[OffsetDateTime] =
    Try(OffsetDateTime.parse(value.trim, dateFormatter)).toOption
}

