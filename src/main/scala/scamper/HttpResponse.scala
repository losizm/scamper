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
trait HttpResponse extends HttpMessage {
  type MessageType = HttpResponse
  type LineType = StatusLine
  type CookieType = SetCookie

  /** Response status */
  def status: ResponseStatus = startLine.status

  /** HTTP version */
  def version: HttpVersion = startLine.version

  /**
   * Creates new response replacing status.
   *
   * @return new response
   */
  def withStatus(status: ResponseStatus): MessageType

  /**
   * Creates new response replacing version.
   *
   * @return new response
   */
  def withVersion(version: HttpVersion): MessageType
}

/** HttpResponse factory */
object HttpResponse {
  /** Creates HttpResponse using supplied attributes. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    HttpResponseImpl(statusLine, headers, body)

  /** Creates HttpResponse using supplied attributes. */
  def apply(status: ResponseStatus, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: HttpVersion = HttpVersion(1, 1)): HttpResponse =
    HttpResponseImpl(StatusLine(status, version), headers, body)
}

private case class HttpResponseImpl(startLine: StatusLine, headers: Seq[Header], body: Entity) extends HttpResponse {
  lazy val cookies: Seq[SetCookie] =
    getHeaderValues("Set-Cookie").map(SetCookie(_))

  def withHeaders(newHeaders: Header*): HttpResponse =
    copy(headers = newHeaders)

  def withCookies(newCookies: SetCookie*): HttpResponse =
    copy(headers = headers.filterNot(_.key.equalsIgnoreCase("Set-Cookie")) ++ newCookies.map(c => Header("Set-Cookie", c.toString)))

  def withBody(newBody: Entity): HttpResponse =
    copy(body = newBody)

  def withStartLine(line: StatusLine) =
    copy(startLine = line)

  def withStatus(newStatus: ResponseStatus): HttpResponse =
    copy(startLine = StatusLine(newStatus, version))

  def withVersion(newVersion: HttpVersion): HttpResponse =
    copy(startLine = StatusLine(status, newVersion))
}
