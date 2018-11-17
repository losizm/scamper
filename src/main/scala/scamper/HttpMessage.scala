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
  /** Type of start line used in message */
  type LineType <: StartLine

  /** Gets message start line. */
  def startLine: LineType

  /** Gets HTTP version. */
  def version: HttpVersion = startLine.version

  /** Gets message headers. */
  def headers: Seq[Header]

  /** Gets message body. */
  def body: Entity

  /** Parses message body as defined type. */
  def parse[T](implicit bodyParser: BodyParser[T]): T =
    bodyParser.parse(this)

  /** Tests whether header with given name is present. */
  def hasHeader(name: String): Boolean =
    headers.exists(_.name.equalsIgnoreCase(name))

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
}
