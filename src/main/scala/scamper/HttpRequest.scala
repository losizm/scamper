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

import java.net.URI
import Auxiliary.UriType

/**
 * HTTP request
 *
 * @see [[HttpResponse]]
 */
trait HttpRequest extends HttpMessage with MessageBuilder[HttpRequest] {
  type LineType = RequestLine

  /** Gets request method. */
  def method: RequestMethod = startLine.method

  /** Gets request target. */
  def target: URI = startLine.target

  /** Gets target path. */
  def path: String

  /** Gets query string. */
  def query: QueryString

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
   * Creates request with new query.
   *
   * @return new request
   */
  def withQuery(query: QueryString): HttpRequest

  /**
   * Creates request with new query parameters.
   *
   * @return new request
   */
  def withQuery(params: Map[String, Seq[String]]): HttpRequest

  /**
   * Creates request with new query parameters.
   *
   * @return new request
   */
  def withQuery(params: (String, String)*): HttpRequest

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

private case class HttpRequestImpl(startLine: RequestLine, headers: Seq[Header], body: Entity, attributes: Map[String, Any] = Map.empty) extends HttpRequest {
  lazy val path: String = target.getRawPath match {
    case "" =>
      if (method.name == "OPTIONS")
        "*"
      else
        "/"
    case path => path
  }

  lazy val query: QueryString =
    target.getRawQuery match {
      case null  => QueryString.empty
      case query => QueryString(query)
    }

  def withStartLine(newStartLine: RequestLine): HttpRequest =
    copy(startLine = newStartLine)

  def withMethod(newMethod: RequestMethod): HttpRequest =
    copy(startLine = RequestLine(newMethod, target, version))

  def withTarget(newTarget: URI): HttpRequest =
    copy(startLine = RequestLine(method, newTarget, version))

  def withPath(newPath: String): HttpRequest =
    newPath match {
      case "*" if method.name == "OPTIONS" => withTarget(target.withPath(""))
      case _   => withTarget(target.withPath(newPath))
    }

  def withQuery(query: QueryString): HttpRequest =
    withTarget(target.withQuery(query.toString))

  def withQuery(params: Map[String, Seq[String]]): HttpRequest =
    withQuery(QueryString(params))

  def withQuery(params: (String, String)*): HttpRequest =
    withQuery(QueryString(params : _*))

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

  def withBody(newBody: Entity): HttpRequest =
    copy(body = newBody)

  def withAttributes(newAttributes: (String, Any)*): HttpRequest =
    copy(attributes = newAttributes.toMap)

  def withAttribute(attribute: (String, Any)): HttpRequest =
    copy(attributes = attributes + attribute)

  def removeAttribute(name: String): HttpRequest =
    copy(attributes = attributes - name)
}
