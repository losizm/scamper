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

/** Provides builder pattern for HTTP message. */
trait MessageBuilder[T <: HttpMessage] { this: T =>
  /**
   * Creates message with supplied start line.
   *
   * @param startLine message start line
   *
   * @return new message
   */
  def setStartLine(startLine: T#LineType): T

  /**
   * Creates message with supplied headers.
   *
   * @param headers message headers
   *
   * @return new message
   *
   * @note All previous headers are removed.
   */
  def setHeaders(headers: Seq[Header]): T

  /**
   * Creates message with supplied headers.
   *
   * @param one message header
   * @param more additional message headers
   *
   * @return new message
   *
   * @note All previous headers are removed.
   */
  def setHeaders(one: Header, more: Header*): T =
    setHeaders(one +: more)

  /**
   * Creates message with additional headers.
   *
   * @param headers message headers
   *
   * @return new message
   */
  def addHeaders(headers: Seq[Header]): T =
    headers.isEmpty match {
      case true  => this
      case false => setHeaders(this.headers ++ headers)
    }

  /**
   * Creates message with additional headers.
   *
   * @param one message header
   * @param more additional message headers
   *
   * @return new message
   */
  def addHeaders(one: Header, more: Header*): T =
    addHeaders(one +: more)

  /**
   * Creates message with supplied headers.
   *
   * @param headers message headers
   *
   * @return new message
   *
   * @note All previous headers with same name are removed.
   */
  def putHeaders(headers: Seq[Header]): T =
    headers.isEmpty match {
      case true  => this
      case false =>
        val names = headers.map(_.name).distinct
        setHeaders { this.headers.filterNot(h => names.exists(h.name.equalsIgnoreCase)) ++ headers }
    }

  /**
   * Creates message with supplied headers.
   *
   * @param one header
   * @param more additional headers
   *
   * @return new message
   *
   * @note All previous headers with same name are removed.
   */
  def putHeaders(one: Header, more: Header*): T =
    putHeaders(one +: more)

  /**
   * Creates message excluding headers with given names.
   *
   * @param names header names
   *
   * @return new message
   */
  def removeHeaders(names: Seq[String]): T =
    names.isEmpty match {
      case true  => this
      case false => setHeaders { headers.filterNot(h => names.exists(h.name.equalsIgnoreCase)) }
    }

  /**
   * Creates message excluding headers with given names.
   *
   * @param one header name
   * @param more additional header names
   *
   * @return new message
   */
  def removeHeaders(one: String, more: String*): T =
    removeHeaders(one +: more)

  /**
   * Creates message with supplied body.
   *
   * @param body message body
   *
   * @return new message
   */
  def setBody(body: Entity): T

  /**
   * Creates message with supplied attributes.
   *
   * @param attributes message attributes
   *
   * @return new message
   *
   * @note All previous attributes are removed.
   */
  def setAttributes(attributes: Map[String, Any]): T

  /**
   * Creates message with supplied attributes.
   *
   * @param one message attribute
   * @param more additional message attributes
   *
   * @return new message
   *
   * @note All previous attributes are removed.
   */
  def setAttributes(one: (String, Any), more: (String, Any)*): T =
    setAttributes((one +: more).toMap)

  /**
   * Creates message with supplied attribute.
   *
   * @param attributes attributes
   *
   * @return new message
   *
   * @note If attribute already exists, its value is replaced.
   */
  def putAttributes(attributes: Map[String, Any]): T =
    attributes.isEmpty match {
      case true  => this
      case false => setAttributes(this.attributes ++ attributes)
    }

  /**
   * Creates message with supplied attributes.
   *
   * @param one attribute
   * @param more additional attribute
   *
   * @return new message
   *
   * @note If attribute already exists, its value is replaced.
   */
  def putAttributes(one: (String, Any), more: (String, Any)*): T =
    putAttributes((one +: more).toMap)

  /**
   * Creates message excluding attributes with given names.
   *
   * @param names attribute names
   *
   * @return new message
   */
  def removeAttributes(names: Seq[String]): T =
    names.isEmpty match {
      case true  => this
      case false => setAttributes { attributes.filterNot(a => names.contains(a._1)) }
    }

  /**
   * Creates message excluding attributes with given names.
   *
   * @param one attribute name
   * @param more additional attribute names
   *
   * @return new message
   */
  def removeAttributes(one: String, more: String*): T =
    removeAttributes(one +: more)
}
