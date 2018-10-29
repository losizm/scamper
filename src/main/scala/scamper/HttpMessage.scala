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
   * Gets header for specified key.
   *
   * If there are multiple headers for key, then first occurrence is retrieved.
   */
  def getHeader(key: String): Option[Header] =
    headers.find(_.key.equalsIgnoreCase(key))

  /**
   * Gets header for specified key or returns default if header not present.
   *
   * If there are multiple headers for key, then first occurrence is retrieved.
   */
  def getHeaderOrElse(key: String, default: => Header): Header =
    getHeader(key).getOrElse(default)

  /**
   * Gets header value for specified key.
   *
   * If there are multiple headers for key, then value of first occurrence is
   * retrieved.
   */
  def getHeaderValue(key: String): Option[String] =
    getHeader(key).map(_.value)

  /**
   * Gets header value for specified key or returns default if header not
   * present.
   *
   * If there are multiple headers for key, then value of first occurrence is
   * retrieved.
   */
  def getHeaderValueOrElse(key: String, default: => String): String =
    getHeaderValue(key).getOrElse(default)

  /** Gets all headers for specified key. */
  def getHeaders(key: String): Seq[Header] =
    headers.filter(_.key.equalsIgnoreCase(key))

  /** Gets all header values for specified key. */
  def getHeaderValues(key: String): Seq[String] =
    getHeaders(key).map(_.value)

  /** Gets cookie for specified name. */
  def getCookie(name: String): Option[CookieType] =
    cookies.find(_.name == name)

  /** Gets cookie value for specified name. */
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
   * Previous headers having same key as supplied header are removed.
   *
   * @return new message
   */
  def withHeader(header: Header): MessageType =
    withHeaders(headers.filterNot(_.key.equalsIgnoreCase(header.key)) :+ header : _*)

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
   * Creates message removing headers having specified keys.
   *
   * @return new message
   */
  def removeHeaders(keys: String*): MessageType =
    withHeaders(headers.filterNot(header => keys.exists(header.key.equalsIgnoreCase)) : _*)

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
