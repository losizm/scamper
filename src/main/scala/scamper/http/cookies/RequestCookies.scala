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

/**
 * Provides access to request cookies in Cookie header.
 *
 * In [[HttpRequest]], cookies are stringed together in the Cookie header. You
 * can access them using extension methods provided by [[RequestCookies]], with
 * each cookie represented as [[PlainCookie]].
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.http.RequestMethod.Registry.Get
 * import scamper.http.cookies.{ PlainCookie, RequestCookies }
 * import scamper.http.stringToUri
 *
 * // Build request with cookies
 * val req = Get("https://localhost:8080/motd").setCookies(
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
 */
implicit class RequestCookies(request: HttpRequest) extends AnyVal:
  /** Gets cookies. */
  def cookies: Seq[PlainCookie] =
    request.getHeaderValue("Cookie")
      .map(PlainCookie.parseAll)
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
   *
   * @note All previous cookies are removed.
   */
  def setCookies(cookies: Seq[PlainCookie]): HttpRequest =
    cookies.isEmpty match
      case true  => request.removeHeaders("Cookie")
      case false => request.putHeaders(Header("Cookie", cookies.mkString("; ")))

  /**
   * Creates copy of request with new set of cookies.
   *
   * @param one new cookie
   * @param more additional new cookies
   *
   * @note All previous cookies are removed.
   */
  def setCookies(one: PlainCookie, more: PlainCookie*): HttpRequest =
    setCookies(one +: more)

  /**
   * Creates copy of request with supplied cookie.
   *
   * @param cookies new cookies
   *
   * @note Previous cookies with same name are removed.
   */
  def putCookies(cookies: Seq[PlainCookie]): HttpRequest =
    cookies.isEmpty match
      case true  => request
      case false =>
        val names = cookies.map(_.name)
        setCookies(this.cookies.filterNot(c => names.contains(c.name)) ++ cookies)

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
    setCookies(cookies.filterNot(cookie => names.contains(cookie.name)))

  /**
   * Creates copy of request excluding cookies with given names.
   *
   * @param one cookie name
   * @param more additional cookie names
   */
  def removeCookies(one: String, more: String*): HttpRequest =
    removeCookies(one +: more)
