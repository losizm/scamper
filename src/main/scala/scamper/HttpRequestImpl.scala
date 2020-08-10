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

import Auxiliary.UriType
import Validate.{ noNulls, notNull }

private case class HttpRequestImpl(startLine: RequestLine, headers: Seq[Header], body: Entity, attributes: Map[String, Any] = Map.empty) extends HttpRequest {
  notNull(startLine)
  noNulls(headers, "headers cannot contain null header")
  notNull(body)
  notNull(attributes)

  lazy val path: String = target.normalize.getRawPath match {
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

  def withTarget(newTarget: Uri): HttpRequest =
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

  def withQuery(params: Seq[(String, String)]): HttpRequest =
    withQuery(QueryString(params))

  def withVersion(newVersion: HttpVersion): HttpRequest =
    copy(startLine = RequestLine(method, target, newVersion))

  def withHeaders(newHeaders: Seq[Header]): HttpRequest =
    copy(headers = newHeaders)

  def addHeaders(newHeaders: Seq[Header]): HttpRequest =
    withHeaders(headers ++ newHeaders)

  def removeHeaders(names: Seq[String]): HttpRequest =
    withHeaders(headers.filterNot(header => names.exists(header.name.equalsIgnoreCase)))

  def withHeader(header: Header): HttpRequest =
    withHeaders(headers.filterNot(_.name.equalsIgnoreCase(header.name)) :+ header)

  def addOptionalHeader(name: String, value: Option[String]): HttpRequest =
    value.map(value => Header(name, value))
      .map(addHeaders(_))
      .getOrElse(this)

  def withBody(newBody: Entity): HttpRequest =
    copy(body = newBody)

  def withAttributes(newAttributes: Map[String, Any]): HttpRequest =
    copy(attributes = newAttributes)

  def withAttribute(attribute: (String, Any)): HttpRequest =
    copy(attributes = attributes + attribute)

  def removeAttributes(names: Seq[String]): HttpRequest =
    copy(attributes = attributes.filterNot(x => names.contains(x._1)))
}
