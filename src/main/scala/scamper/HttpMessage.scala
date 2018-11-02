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

/** HTTP message */
trait HttpMessage {
  /** Type of HTTP message */
  type MessageType <: HttpMessage

  /** Type of start line used in message */
  type LineType <: StartLine

  /** Type of cookie used in message */
  type CookieType <: Cookie

  /** Gets message start line. */
  def startLine: LineType

  /** Gets HTTP version. */
  def version: HttpVersion = startLine.version

  /** Gets message headers. */
  def headers: Seq[Header]

  /** Gets message cookies. */
  def cookies: Seq[CookieType]

  /** Gets message body. */
  def body: Entity

  /** Parses message body as defined type. */
  def parse[T](implicit bodyParser: BodyParser[T]): T =
    bodyParser.parse(this)

  /**
   * Gets header with given name.
   *
   * If there are multiple headers with name, then first occurrence is
   * retrieved.
   */
  def getHeader(name: String): Option[Header] =
    headers.find(_.name.equalsIgnoreCase(name))

  /**
   * Gets header with given name or returns default if header not present.
   *
   * If there are multiple headers with name, then first occurrence is
   * retrieved.
   */
  def getHeaderOrElse(name: String, default: => Header): Header =
    getHeader(name).getOrElse(default)

  /**
   * Gets value of header with given name.
   *
   * If there are multiple headers with name, then first occurrence is
   * retrieved.
   */
  def getHeaderValue(name: String): Option[String] =
    getHeader(name).map(_.value)

  /**
   * Gets value of header with given name or returns default if header not
   * present.
   *
   * If there are multiple headers with name, then first occurrence is
   * retrieved.
   */
  def getHeaderValueOrElse(name: String, default: => String): String =
    getHeaderValue(name).getOrElse(default)

  /** Gets all headers with given name. */
  def getHeaders(name: String): Seq[Header] =
    headers.filter(_.name.equalsIgnoreCase(name))

  /** Gets all values of headers with given name. */
  def getHeaderValues(name: String): Seq[String] =
    getHeaders(name).map(_.value)

  /** Gets cookie with given name. */
  def getCookie(name: String): Option[CookieType] =
    cookies.find(_.name == name)

  /** Gets value of cookie with given name. */
  def getCookieValue(name: String): Option[String] =
    getCookie(name).map(_.value)

  /**
   * Creates message with new start line.
   *
   * @return new message
   */
  def withStartLine(line: LineType): MessageType

  /**
   * Creates message with supplied header.
   *
   * Previous headers having same name as supplied header are removed.
   *
   * @return new message
   */
  def withHeader(header: Header): MessageType =
    withHeaders(headers.filterNot(_.name.equalsIgnoreCase(header.name)) :+ header : _*)

  /**
   * Creates message with new headers.
   *
   * All previous headers are removed.
   *
   * @return new message
   */
  def withHeaders(headers: Header*): MessageType

  /**
   * Creates message with additional headers.
   *
   * @return new message
   */
  def addHeaders(headers: Header*): MessageType =
    withHeaders(this.headers ++ headers : _*)

  /**
   * Creates message removing headers with given names.
   *
   * @return new message
   */
  def removeHeaders(names: String*): MessageType =
    withHeaders(headers.filterNot(header => names.exists(header.name.equalsIgnoreCase)) : _*)

  /**
   * Creates message with new cookies.
   *
   * All previous cookies are removed.
   *
   * @return new message
   */
  def withCookies(cookies: CookieType*): MessageType

  /**
   * Creates message with new body.
   *
   * @return new message
   */
  def withBody(body: Entity): MessageType
}
