/*
 * Copyright 2018 Carlos Conyers
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

import java.time.OffsetDateTime
import scala.util.Try

package object cookies {
  private object CookieHelper {
    private val CookieValue = new Grammar("([\\x21-\\x7E&&[^\",;\\\\]]*)".r)
    private val QuotedCookieValue = new Grammar("\"([\\x21-\\x7E&&[^\",;\\\\]]*)\"".r)

    def Name(name: String): String =
      Grammar.Token(name) getOrElse {
        throw new IllegalArgumentException(s"Invalid cookie name: $name")
      }

    def Value(value: String): String =
      CookieValue(value) orElse QuotedCookieValue(value) getOrElse {
        throw new IllegalArgumentException(s"Invalid cookie value: $value")
      }
  }
  import CookieHelper._

  /** HTTP Cookie */
  trait Cookie {
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

  /** PlainCookie factory */
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
    def expires: Option[OffsetDateTime]

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

  /** SetCookie factory */
  object SetCookie {
    /** Parses formatted cookie. */
    def parse(cookie: String): SetCookie =
      cookie.split(";", 2) match {
        case Array(pair, attribs) =>
          pair.split("=") match {
            case Array(name, value) => SetCookieImpl(Name(name), Value(value), CookieAttributes(attribs))
          }
        case Array(pair) =>
          pair.split("=") match {
            case Array(name, value) => SetCookieImpl(Name(name), Value(value), CookieAttributes())
          }
      }

    /** Creates SetCookie with supplied name, value, and attributes. */
    def apply(name: String, value: String, domain: Option[String] = None, path: Option[String] = None, expires: Option[OffsetDateTime] = None,
        maxAge: Option[Long] = None, secure: Boolean = false, httpOnly: Boolean = false): SetCookie =
      SetCookieImpl(Name(name), Value(value), CookieAttributes(domain, path, expires, maxAge, secure, httpOnly))

    /** Destructures SetCookie. */
    def unapply(cookie: SetCookie): Option[(String, String, Option[String], Option[String], Option[OffsetDateTime], Option[Long], Boolean, Boolean)] =
      Some((cookie.name, cookie.value, cookie.domain, cookie.path, cookie.expires, cookie.maxAge, cookie.secure, cookie.httpOnly))
  }

  /** Provides access to request cookies in `Cookie` header. */
  implicit class RequestCookies(val request: HttpRequest) extends AnyVal {
    /** Gets cookies. */
    def cookies: Seq[PlainCookie] =
      request.getHeaderValue("Cookie")
        .map(ListParser(_, semicolon = true))
        .map(_.map(PlainCookie.parse).toSeq)
        .getOrElse(Nil)

    /**
     * Gets specified cookie.
     *
     * @param name cookie name
     */
    def getCookie(name: String): Option[PlainCookie] =
      cookies.find(_.name == name)

    /**
     * Gets value of specified cookie.
     *
     * @param name cookie name
     */
    def getCookieValue(name: String): Option[String] =
      getCookie(name).map(_.value)

    /**
     * Creates copy of message with new set of cookies.
     *
     * @param cookies new set of cookies
     */
    def withCookies(cookies: PlainCookie*): HttpRequest =
      request.withHeaders({
        request.headers.filterNot(_.name.equalsIgnoreCase("Cookie")) :+ Header("Cookie", cookies.mkString("; "))
      } : _*)
  }

  /** Provides access to response cookies in `Set-Cookie` headers. */
  implicit class ResponseCookies(val response: HttpResponse) extends AnyVal {
    /** Gets cookies. */
    def cookies: Seq[SetCookie] =
      response.getHeaderValues("Set-Cookie").map(SetCookie.parse)

    /**
     * Gets specified cookie.
     *
     * @param name cookie name
     */
    def getCookie(name: String): Option[SetCookie] =
      cookies.find(_.name == name)

    /**
     * Gets value of specified cookie.
     *
     * @param name cookie name
     */
    def getCookieValue(name: String): Option[String] =
      getCookie(name).map(_.value)

    /**
     * Creates copy of message with new set of cookies.
     *
     * @param cookies new set of cookies
     */
    def withCookies(cookies: SetCookie*): HttpResponse =
      response.withHeaders({
        response.headers.filterNot(_.name.equalsIgnoreCase("Set-Cookie")) ++ cookies.map(c => Header("Set-Cookie", c.toString))
      } : _*)
  }

  private case class PlainCookieImpl(name: String, value: String) extends PlainCookie

  private case class SetCookieImpl(name: String, value: String, attribs: CookieAttributes) extends SetCookie {
    def domain: Option[String] = attribs.domain
    def path: Option[String] = attribs.path
    def expires: Option[OffsetDateTime] = attribs.expires
    def maxAge: Option[Long] = attribs.maxAge
    def secure: Boolean = attribs.secure
    def httpOnly: Boolean = attribs.httpOnly
  }

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
      Try(DateValue.parse(value.trim)).toOption
  }
}
