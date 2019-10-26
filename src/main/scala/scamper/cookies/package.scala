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
package scamper

import java.time.Instant
import scala.util.Try

/**
 * Provides specialized access to message cookies.
 *
 * === Request Cookies ===
 *
 * In [[HttpRequest]], cookies are stringed together in the '''Cookie''' header.
 * You can access them using the extension methods provided by [[RequestCookies]],
 * with each cookie represented as [[PlainCookie]].
 *
 * {{{
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.GET
 * import scamper.cookies.{ PlainCookie, RequestCookies }
 *
 * // Build request with cookies
 * val req = GET("https://localhost:8080/motd").withCookies(
 *   PlainCookie("ID", "bG9zCg"), PlainCookie("Region", "SE-US")
 * )
 *
 * // Access and print all cookies
 * req.cookies.foreach(println)
 *
 * // Get cookies by name
 * val id: Option[PlainCookie] = req.getCookie("ID")
 * val region: Option[PlainCookie] = req.getCookie("Region")
 *
 * // Get cookie values by name
 * assert(req.getCookieValue("ID").contains("bG9zCg"))
 * assert(req.getCookieValue("Region").contains("SE-US"))
 * }}}
 *
 * === Response Cookies ===
 *
 * In [[HttpResponse]], the cookies are a collection of '''Set-Cookie'''
 * header values.  Specialized access is provided by [[ResponseCookies]], with
 * each cookie represented as [[SetCookie]].
 *
 * {{{
 * import scamper.Implicits.stringToEntity
 * import scamper.ResponseStatus.Registry.Ok
 * import scamper.cookies.{ ResponseCookies, SetCookie }
 *
 * // Build response with cookies
 * val res = Ok("There is an answer.").withCookies(
 *   SetCookie("ID", "bG9zCg", path = Some("/motd"), secure = true),
 *   SetCookie("Region", "SE-US")
 * )
 *
 * // Access and print all cookies
 * res.cookies.foreach(println)
 *
 * // Get cookies by name
 * val id: Option[SetCookie] = res.getCookie("ID")
 * val region: Option[SetCookie] = res.getCookie("Region")
 *
 * // Get attributes of ID cookie
 * val path: String = id.flatMap(_.path).getOrElse("/")
 * val secure: Boolean = id.map(_.secure).getOrElse(false)
 *
 * // Get cookie values by name
 * assert(res.getCookieValue("ID").contains("bG9zCg"))
 * assert(res.getCookieValue("Region").contains("SE-US"))
 * }}}
 */
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

  /** Provides access to request cookies in `Cookie` header. */
  implicit class RequestCookies(private val request: HttpRequest) extends AnyVal {
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
     * Creates copy of request with supplied cookie.
     *
     * If a cookie already exists with given name, the existing cookie is
     * replaced with the new cookie.
     *
     * @param cookies new cookie
     */
    def withCookie(cookie: PlainCookie): HttpRequest =
      withCookies({ cookies.filterNot(_.name == cookie.name) :+ cookie } : _*)

    /**
     * Creates copy of request with new set of cookies.
     *
     * @param cookies new set of cookies
     */
    def withCookies(cookies: PlainCookie*): HttpRequest =
      if (cookies.isEmpty) request.removeHeaders("Cookie")
      else request.withHeader(Header("Cookie", cookies.mkString("; ")))

    /**
     * Creates copy of request excluding cookies with given names.
     *
     * @param names cookie names
     */
    def removeCookies(names: String*): HttpRequest =
      withCookies { cookies.filterNot { cookie => names.contains(cookie.name) } : _* }
  }

  /** Provides access to response cookies in `Set-Cookie` headers. */
  implicit class ResponseCookies(private val response: HttpResponse) extends AnyVal {
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
     * Creates copy of response with supplied cookie.
     *
     * If a cookie already exists with given name, the existing cookie is
     * replaced with the new cookie.
     *
     * @param cookies new cookie
     */
    def withCookie(cookie: SetCookie): HttpResponse =
      withCookies({ cookies.filterNot(_.name == cookie.name) :+ cookie } : _*)

    /**
     * Creates copy of response with new set of cookies.
     *
     * @param cookies new set of cookies
     */
    def withCookies(cookies: SetCookie*): HttpResponse =
      response.withHeaders({
        response.headers.filterNot(_.name.equalsIgnoreCase("Set-Cookie")) ++ cookies.map(c => Header("Set-Cookie", c.toString))
      } : _*)

    /**
     * Creates copy of response excluding cookies with given names.
     *
     * @param names cookie names
     */
    def removeCookies(names: String*): HttpResponse =
      withCookies { cookies.filterNot { cookie => names.contains(cookie.name) } : _* }
  }

  private case class PlainCookieImpl(name: String, value: String) extends PlainCookie

  private case class SetCookieImpl(name: String, value: String, attrs: CookieAttributes) extends SetCookie {
    def domain: Option[String] = attrs.domain
    def path: Option[String] = attrs.path
    def expires: Option[Instant] = attrs.expires
    def maxAge: Option[Long] = attrs.maxAge
    def secure: Boolean = attrs.secure
    def httpOnly: Boolean = attrs.httpOnly
  }

  private case class CookieAttributes(domain: Option[String] = None, path: Option[String] = None, expires: Option[Instant] = None,
      maxAge: Option[Long] = None, secure: Boolean = false, httpOnly: Boolean = false)

  private object CookieAttributes {
    def parse(attrs: String): CookieAttributes =
      attrs.split(";").map(_.split("=", 2).map(_.trim.toLowerCase)).foldRight(CookieAttributes())(append)

    private def append(attr: Array[String], attrs: CookieAttributes): CookieAttributes =
      attr match {
        case Array(name, value) if name == "domain"   => attrs.copy(domain = Some(value))
        case Array(name, value) if name == "path"     => attrs.copy(path = Some(value))
        case Array(name, value) if name == "expires"  => attrs.copy(expires = toExpires(value))
        case Array(name, value) if name == "max-age"  => attrs.copy(maxAge = toMaxAge(value))
        case Array(name)        if name == "secure"   => attrs.copy(secure = true)
        case Array(name)        if name == "httponly" => attrs.copy(httpOnly = true)
        case _ => attrs
      }

    private def toMaxAge(value: String): Option[Long] =
      Try(value.trim.toLong).toOption

    private def toExpires(value: String): Option[Instant] =
      Try(DateValue.parse(value.trim)).toOption
  }
}
