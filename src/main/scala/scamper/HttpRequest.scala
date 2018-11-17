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

import java.net.URI
import scamper.auxiliary.HttpUriType

/**
 * HTTP request
 *
 * @see [[HttpResponse]]
 */
trait HttpRequest extends HttpMessage with MessageBuilder[HttpRequest] {
  type LineType = RequestLine
  type CookieType = PlainCookie

  /** Gets request method. */
  def method: RequestMethod = startLine.method

  /** Gets request target. */
  def target: URI = startLine.target

  /** Gets target path. */
  def path: String

  /** Gets query parameters. */
  def queryParams: Map[String, Seq[String]]

  /**
   * Gets value for named query parameter.
   *
   * If there are multiple parameters with given name, then value of first
   * occurrence is retrieved.
   */
  def getQueryParamValue(name: String): Option[String]

  /** Gets all values for named query parameter. */
  def getQueryParamValues(name: String): Seq[String]

  /**
   * Creates request with new method.
   *
   * @return new request
   */
  def withMethod(method: RequestMethod): HttpRequest

  /**
   * Creates request with new target.
   *
   * @return new request
   */
  def withTarget(target: URI): HttpRequest

  /**
   * Creates request with new target path.
   *
   * @return new request
   */
  def withPath(path: String): HttpRequest

  /**
   * Creates request with new query parameters.
   *
   * @return new request
   */
  def withQueryParams(params: Map[String, Seq[String]]): HttpRequest

  /**
   * Creates request with new query parameters.
   *
   * @return new request
   */
  def withQueryParams(params: (String, String)*): HttpRequest

  /**
   * Creates request with new HTTP version.
   *
   * @return new request
   */
  def withVersion(version: HttpVersion): HttpRequest
}

/** HttpRequest factory */
object HttpRequest {
  /** Creates HttpRequest with supplied values. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    HttpRequestImpl(requestLine, headers, body)

  /** Creates HttpRequest with supplied values. */
  def apply(method: RequestMethod, target: URI = new URI("/"), headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: HttpVersion = HttpVersion(1, 1)): HttpRequest =
    HttpRequestImpl(RequestLine(method, target, version), headers, body)
}

private case class HttpRequestImpl(startLine: RequestLine, headers: Seq[Header], body: Entity) extends HttpRequest {
  lazy val path: String = target.getRawPath

  lazy val queryParams: Map[String, Seq[String]] =
    target.getRawQuery match {
      case null  => Map.empty
      case query => QueryParams.parse(query)
    }

  lazy val cookies: Seq[PlainCookie] =
    getHeaderValue("Cookie")
      .map(ListParser(_, semicolon = true))
      .map(_.map(PlainCookie.parse).toSeq)
      .getOrElse(Nil)

  def getQueryParamValue(name: String): Option[String] =
    queryParams.get(name).flatMap(_.headOption)

  def getQueryParamValues(name: String): Seq[String] =
    queryParams.getOrElse(name, Nil)

  def withStartLine(newStartLine: RequestLine): HttpRequest =
    copy(startLine = newStartLine)

  def withMethod(newMethod: RequestMethod): HttpRequest =
    copy(startLine = RequestLine(newMethod, target, version))

  def withTarget(newTarget: URI): HttpRequest =
    copy(startLine = RequestLine(method, newTarget, version))

  def withPath(newPath: String): HttpRequest =
    withTarget(target.withPath(newPath))

  def withQueryParams(params: Map[String, Seq[String]]): HttpRequest =
    withTarget(target.withQueryParams(params))

  def withQueryParams(params: (String, String)*): HttpRequest =
    withTarget(target.withQueryParams(params : _*))

  def withVersion(newVersion: HttpVersion): HttpRequest =
    copy(startLine = RequestLine(method, target, newVersion))

  def withHeaders(newHeaders: Header*): HttpRequest =
    copy(headers = newHeaders)

  def addHeaders(newHeaders: Header*): HttpRequest =
    withHeaders(headers ++ newHeaders : _*)

  def removeHeaders(names: String*): HttpRequest =
    withHeaders(headers.filterNot(header => names.exists(header.name.equalsIgnoreCase)) : _*)

  def withHeader(header: Header): HttpRequest =
    withHeaders(headers.filterNot(_.name.equalsIgnoreCase(header.name)) :+ header : _*)

  def withCookies(newCookies: PlainCookie*): HttpRequest =
    copy(headers = headers.filterNot(_.name.equalsIgnoreCase("Cookie")) :+ Header("Cookie", newCookies.mkString("; ")))

  def withBody(newBody: Entity): HttpRequest =
    copy(body = newBody)
}
