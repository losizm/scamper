/*
 * Copyright 2017-2020 Carlos Conyers
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
 * You can access them using extension methods provided by [[RequestCookies]],
 * with each cookie represented as [[PlainCookie]].
 *
 * {{{
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.cookies.{ PlainCookie, RequestCookies }
 *
 * // Build request with cookies
 * val req = Get("https://localhost:8080/motd").withCookies(
 *   PlainCookie("ID", "bG9zCg"), PlainCookie("Region", "SE-US")
 * )
 *
 * // Print all cookies
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
 * // Print all cookies
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
     * Creates copy of request with new set of cookies.
     *
     * @param cookies new set of cookies
     */
    def withCookies(cookies: Seq[PlainCookie]): HttpRequest =
      cookies.isEmpty match {
        case true  => request.removeHeaders("Cookie")
        case false => request.putHeaders(Header("Cookie", cookies.mkString("; ")))
      }

    /**
     * Creates copy of request with new set of cookies.
     *
     * @param one new cookie
     * @param more additional new cookies
     */
    def withCookies(one: PlainCookie, more: PlainCookie*): HttpRequest =
      withCookies(one +: more)

    /**
     * Creates copy of request with supplied cookie.
     *
     * @param cookies new cookies
     *
     * @note Previous cookies with same name are removed.
     */
    def putCookies(cookies: Seq[PlainCookie]): HttpRequest =
      cookies.isEmpty match {
        case true  => request
        case false =>
          val names = cookies.map(_.name)
          withCookies(this.cookies.filterNot(c => names.contains(c.name)) ++ cookies)
      }


    /**
     * Creates copy of request with supplied cookies.
     *
     * @param one cookie
     * @param more additional cookies
     *
     * @note Previous cookies with same name are removed.
     */
    def putCookies(one: PlainCookie, more: PlainCookie*): HttpRequest =
      putCookies(one +: more)

    /**
     * Creates copy of request excluding cookies with given names.
     *
     * @param names cookie names
     */
    def removeCookies(names: Seq[String]): HttpRequest =
      withCookies(cookies.filterNot(cookie => names.contains(cookie.name)))

    /**
     * Creates copy of request excluding cookies with given names.
     *
     * @param one cookie name
     * @param more additional cookie names
     */
    def removeCookies(one: String, more: String*): HttpRequest =
      removeCookies(one +: more)
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
     * Creates copy of response with new set of cookies.
     *
     * @param cookies new set of cookies
     */
    def withCookies(cookies: Seq[SetCookie]): HttpResponse =
      cookies.isEmpty match {
        case true  => response.removeHeaders("Set-Cookie")
        case false => response.putHeaders(cookies.map(c => Header("Set-Cookie", c.toString)))
      }

    /**
     * Creates copy of response with new set of cookies.
     *
     * @param one new cookie
     * @param more additional new cookies
     */
    def withCookies(one: SetCookie, more: SetCookie*): HttpResponse =
      withCookies(one +: more)

    /**
     * Creates copy of response with supplied cookies.
     *
     * @param cookies new cookies
     *
     * @note Previous cookies with same name are removed.
     */
    def putCookies(cookies: Seq[SetCookie]): HttpResponse =
      cookies.isEmpty match {
        case true  => response
        case false =>
          val names = cookies.map(_.name)
          withCookies(this.cookies.filterNot(c => names.contains(c.name)) ++ cookies)
      }

    /**
     * Creates copy of response with supplied cookies.
     *
     * @param one cookie
     * @param more additional cookies
     *
     * @note Previous cookies with same name are removed.
     */
    def putCookies(one: SetCookie, more: SetCookie*): HttpResponse =
      putCookies(one +: more)

    /**
     * Creates copy of response excluding cookies with given names.
     *
     * @param names cookie names
     */
    def removeCookies(names: Seq[String]): HttpResponse =
      withCookies(cookies.filterNot(cookie => names.contains(cookie.name)))

    /**
     * Creates copy of response excluding cookies with given names.
     *
     * @param one cookie name
     * @param more additional cookie names
     */
    def removeCookies(one: String, more: String*): HttpResponse =
      removeCookies(one +: more)
  }
}
