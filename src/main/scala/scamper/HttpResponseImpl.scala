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

import Validate.{ noNulls, notNull }

private case class HttpResponseImpl(startLine: StatusLine, headers: Seq[Header], body: Entity, attributes: Map[String, Any] = Map.empty) extends HttpResponse {
  notNull(startLine)
  noNulls(headers, "headers cannot contain null header")
  notNull(body)
  notNull(attributes)

  def withStartLine(newStartLine: StatusLine) =
    copy(startLine = newStartLine)

  def withStatus(newStatus: ResponseStatus): HttpResponse =
    copy(startLine = StatusLine(newStatus, version))

  def withVersion(newVersion: HttpVersion): HttpResponse =
    copy(startLine = StatusLine(status, newVersion))

  def withHeaders(newHeaders: Header*): HttpResponse =
    copy(headers = newHeaders)

  def addHeaders(newHeaders: Header*): HttpResponse =
    withHeaders(headers ++ newHeaders : _*)

  def removeHeaders(names: String*): HttpResponse =
    withHeaders(headers.filterNot(header => names.exists(header.name.equalsIgnoreCase)) : _*)

  def withHeader(header: Header): HttpResponse =
    withHeaders(headers.filterNot(_.name.equalsIgnoreCase(header.name)) :+ header : _*)

  def withBody(newBody: Entity): HttpResponse =
    copy(body = newBody)

  def withAttributes(newAttributes: Map[String, Any]): HttpResponse =
    copy(attributes = newAttributes)

  def withAttribute(attribute: (String, Any)): HttpResponse =
    copy(attributes = attributes + attribute)

  def removeAttribute(name: String): HttpResponse =
    copy(attributes = attributes - name)
}
