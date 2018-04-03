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
import scamper.extensions.URIExtension

/**
 * HTTP request
 *
 * @see [[HttpResponse]]
 */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest
  type LineType = RequestLine
  type CookieType = PlainCookie

  /** Request method */
  def method: RequestMethod = startLine.method

  /** Request URI */
  def uri: String = startLine.uri

  /** HTTP version */
  def version: HttpVersion = startLine.version

  /** URI path component */
  def path: String

  /** Query parameters */
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
  def withMethod(method: RequestMethod): MessageType

  /**
   * Creates request with new URI.
   *
   * @return new request
   */
  def withURI(uri: String): MessageType

  /**
   * Creates request with new URI path component.
   *
   * @return new request
   */
  def withPath(path: String): MessageType

  /**
   * Creates request with new query parameters.
   *
   * @return new request
   */
  def withQueryParams(params: Map[String, Seq[String]]): MessageType

  /**
   * Creates request with new query parameters.
   *
   * @return new request
   */
  def withQueryParams(params: (String, String)*): MessageType

  /**
   * Creates request with new HTTP version.
   *
   * @return new request
   */
  def withVersion(version: HttpVersion): MessageType
}

/** HttpRequest factory */
object HttpRequest {
  /** Creates HttpRequest with supplied values. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    HttpRequestImpl(requestLine, headers, body)

  /** Creates HttpRequest with supplied values. */
  def apply(method: RequestMethod, uri: String = "/", headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: HttpVersion = HttpVersion(1, 1)): HttpRequest =
    HttpRequestImpl(RequestLine(method, uri, version), headers, body)
}

private case class HttpRequestImpl(startLine: RequestLine, headers: Seq[Header], body: Entity) extends HttpRequest {
  private lazy val uriObject = new URI(uri)

  lazy val path: String = uriObject.getRawPath

  lazy val queryParams: Map[String, Seq[String]] =
    uriObject.getRawQuery match {
      case null  => Map.empty
      case query => QueryParams.parse(query)
    }

  lazy val cookies: Seq[PlainCookie] =
    getHeaderValue("Cookie")
      .map(ListParser(_, semicolon = true))
      .map(_.map(PlainCookie(_)).toSeq)
      .getOrElse(Nil)

  def getQueryParamValue(name: String): Option[String] =
    queryParams.get(name).flatMap(_.headOption)

  def getQueryParamValues(name: String): Seq[String] =
    queryParams.getOrElse(name, Nil)

  def withHeaders(newHeaders: Header*): HttpRequest =
    copy(headers = newHeaders)

  def withCookies(newCookies: PlainCookie*): HttpRequest =
    copy(headers = headers.filterNot(_.key.equalsIgnoreCase("Cookie")) :+ Header("Cookie", newCookies.mkString("; ")))

  def withBody(newBody: Entity): HttpRequest =
    copy(body = newBody)

  def withStartLine(newStartLine: RequestLine): HttpRequest =
    copy(startLine = newStartLine)

  def withMethod(newMethod: RequestMethod): HttpRequest =
    copy(startLine = RequestLine(newMethod, uri, version))

  def withURI(newURI: String): HttpRequest =
    copy(startLine = RequestLine(method, newURI, version))

  def withVersion(newVersion: HttpVersion): HttpRequest =
    copy(startLine = RequestLine(method, uri, newVersion))

  def withPath(newPath: String): HttpRequest =
    withURI(uriObject.withPath(newPath).toString)

  def withQueryParams(params: Map[String, Seq[String]]): HttpRequest =
    withURI(uriObject.withQueryParams(params).toString)

  def withQueryParams(params: (String, String)*): HttpRequest =
    withURI(uriObject.withQueryParams(params : _*).toString)
}
