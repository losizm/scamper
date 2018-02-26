package scamper

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => dateFormatter }

import scala.util.Try

private case class CookieAttributes(domain: Option[String] = None, path: Option[String] = None, expires: Option[OffsetDateTime] = None,
    maxAge: Option[Long] = None, secure: Boolean = false, httpOnly: Boolean = false)

private object CookieAttributes {
  def apply(attribs: String): CookieAttributes =
    attribs.split(";").map(_.split("=", 2).map(_.trim.toLowerCase)).foldRight(CookieAttributes())(append)

  private def append(attrib: Array[String], attribs: CookieAttributes): CookieAttributes =
    attrib match {
      case Array(name, value) if name == "domain"   => attribs.copy(domain = Some(value))
      case Array(name, value) if name == "path"     => attribs.copy(path = Some(value))
      case Array(name, value) if name == "expires"  => attribs.copy(expires = toExpires(value))
      case Array(name, value) if name == "max-age"  => attribs.copy(maxAge = toMaxAge(value))
      case Array(name)        if name == "secure"   => attribs.copy(secure = true)
      case Array(name)        if name == "httponly" => attribs.copy(httpOnly = true)
      case _ => attribs
    }

  private def toMaxAge(value: String): Option[Long] =
    Try(value.trim.toLong).toOption

  private def toExpires(value: String): Option[OffsetDateTime] =
    Try(OffsetDateTime.parse(value.trim, dateFormatter)).toOption
}

