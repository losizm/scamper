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

/**
 * HTTP response
 *
 * @see [[HttpRequest]]
 */
trait HttpResponse extends HttpMessage with MessageBuilder[HttpResponse] {
  type LineType = StatusLine

  /** Gets response status. */
  def status: ResponseStatus = startLine.status

  /**
   * Creates response with new response status.
   *
   * @return new response
   */
  def withStatus(status: ResponseStatus): HttpResponse

  /**
   * Creates response with new HTTP version.
   *
   * @return new response
   */
  def withVersion(version: HttpVersion): HttpResponse
}

/** HttpResponse factory */
object HttpResponse {
  /** Creates HttpResponse with supplied values. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    HttpResponseImpl(statusLine, headers, body)

  /** Creates HttpResponse with supplied values. */
  def apply(status: ResponseStatus, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: HttpVersion = HttpVersion(1, 1)): HttpResponse =
    HttpResponseImpl(StatusLine(status, version), headers, body)
}

private case class HttpResponseImpl(startLine: StatusLine, headers: Seq[Header], body: Entity) extends HttpResponse {
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
}
