/*
 * Copyright 2023 Carlos Conyers
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

private case class HttpRequestImpl(startLine: RequestLine, headers: Seq[Header], body: Entity, attributes: Map[String, Any] = Map.empty) extends HttpRequest:
  notNull(startLine, "startLine")
  noNulls(headers, "headers")
  notNull(body, "body")
  notNull(attributes, "attributes")

  def setStartLine(newStartLine: RequestLine): HttpRequest =
    copy(startLine = newStartLine)

  def setHeaders(newHeaders: Seq[Header]): HttpRequest =
    copy(headers = newHeaders)

  def setBody(newBody: Entity): HttpRequest =
    copy(body = newBody)

  def setAttributes(newAttributes: Map[String, Any]): HttpRequest =
    copy(attributes = newAttributes)
