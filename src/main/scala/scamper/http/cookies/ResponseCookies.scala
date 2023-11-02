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

/** Adds standardized access to Set-Cookie headers. */
given toResponseCookies: Conversion[HttpResponse, ResponseCookies] = ResponseCookies(_)

/**
 * Provides access to response cookies in Set-Cookie headers.
 *
 * In [[HttpResponse]], the cookies are a collection of Set-Cookie header
 * values. Specialized access is provided by [[ResponseCookies]], with each
 * cookie represented as [[SetCookie]].
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.http.ResponseStatus.Registry.Ok
 * import scamper.http.cookies.{ ResponseCookies, SetCookie }
 * import scamper.http.stringToEntity
 *
 * // Build response with cookies
 * val res = Ok("There is an answer.").setCookies(
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
class ResponseCookies(response: HttpResponse) extends AnyVal:
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
   *
   * @note All previous cookies are removed.
   */
  def setCookies(cookies: Seq[SetCookie]): HttpResponse =
    cookies.isEmpty match
      case true  => response.removeHeaders("Set-Cookie")
      case false => response.putHeaders(cookies.map(c => Header("Set-Cookie", c.toString)))

  /**
   * Creates copy of response with new set of cookies.
   *
   * @param one new cookie
   * @param more additional new cookies
   *
   * @note All previous cookies are removed.
   */
  def setCookies(one: SetCookie, more: SetCookie*): HttpResponse =
    setCookies(one +: more)

  /**
   * Creates copy of response with supplied cookies.
   *
   * @param cookies new cookies
   *
   * @note Previous cookies with same name are removed.
   */
  def putCookies(cookies: Seq[SetCookie]): HttpResponse =
    cookies.isEmpty match
      case true  => response
      case false =>
        val names = cookies.map(_.name)
        setCookies(this.cookies.filterNot(c => names.contains(c.name)) ++ cookies)

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
    setCookies(cookies.filterNot(cookie => names.contains(cookie.name)))

  /**
   * Creates copy of response excluding cookies with given names.
   *
   * @param one cookie name
   * @param more additional cookie names
   */
  def removeCookies(one: String, more: String*): HttpResponse =
    removeCookies(one +: more)
