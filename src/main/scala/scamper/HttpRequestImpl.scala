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
import RequestMethod.Registry.Options
import Validate.{ noNulls, notNull }

private case class HttpRequestImpl(startLine: RequestLine, headers: Seq[Header], body: Entity, attributes: Map[String, Any] = Map.empty) extends HttpRequest {
  notNull(startLine)
  noNulls(headers, "headers cannot contain null header")
  notNull(body)
  notNull(attributes)

  lazy val path: String = target.normalize.getRawPath match {
    case "" =>
      if (method == Options)
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

  def setStartLine(newStartLine: RequestLine): HttpRequest =
    copy(startLine = newStartLine)

  def setMethod(newMethod: RequestMethod): HttpRequest =
    setStartLine(RequestLine(newMethod, target, version))

  def setTarget(newTarget: Uri): HttpRequest =
    setStartLine(RequestLine(method, newTarget, version))

  def setPath(newPath: String): HttpRequest =
    newPath match {
      case "*" if method == Options => setTarget(target.setPath(""))
      case _   => setTarget(target.setPath(newPath))
    }

  def setQuery(query: QueryString): HttpRequest =
    setTarget(target.setQuery(query.toString))

  def setQuery(params: Map[String, Seq[String]]): HttpRequest =
    setQuery(QueryString(params))

  def setQuery(params: Seq[(String, String)]): HttpRequest =
    setQuery(QueryString(params))

  def setVersion(newVersion: HttpVersion): HttpRequest =
    setStartLine(RequestLine(method, target, newVersion))

  def setHeaders(newHeaders: Seq[Header]): HttpRequest =
    copy(headers = newHeaders)

  def setBody(newBody: Entity): HttpRequest =
    copy(body = newBody)

  def setAttributes(newAttributes: Map[String, Any]): HttpRequest =
    copy(attributes = newAttributes)
}
