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
  def withStartLine(startLine: T#LineType): T

  /**
   * Creates message with supplied headers.
   *
   * @param headers message headers
   *
   * @return new message
   *
   * @note All previous headers are removed.
   */
  def withHeaders(headers: Seq[Header]): T

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
  def withHeaders(one: Header, more: Header*): T =
    withHeaders(one +: more)

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
      case false => withHeaders(this.headers ++ headers)
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
   * Creates message excluding headers with given names.
   *
   * @param names header names
   *
   * @return new message
   */
  def removeHeaders(names: Seq[String]): T =
    names.isEmpty match {
      case true  => this
      case false =>
        withHeaders {
          headers.filterNot(h => names.exists(h.name.equalsIgnoreCase))
        }
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
   * Creates message with supplied header.
   *
   * @param header message header
   *
   * @return new message
   *
   * @note All previous headers with same name are removed.
   */
  def withHeader(header: Header): T =
    withHeaders {
      headers.filterNot(_.name.equalsIgnoreCase(header.name)) :+ header
    }

  /**
   * Creates message with optional header.
   *
   * @param name header name
   * @param value optional header value
   *
   * @return new message
   *
   * @note All previous headers with same name are removed; if no value is
   * supplied, then no header is added.
   */
  def withOptionalHeader(name: String, value: Option[String]): T =
    value.map(value => Header(name, value))
      .map(withHeader)
      .getOrElse(removeHeaders(name))

  /**
   * Creates message with additional header, if value supplied.
   *
   * @param name header name
   * @param value optional header value
   *
   * @return new message
   */
  def addOptionalHeader(name: String, value: Option[String]): T =
    value.map(value => Header(name, value))
      .map(addHeaders(_))
      .getOrElse(this)

  /**
   * Creates message with supplied body.
   *
   * @param body message body
   *
   * @return new message
   */
  def withBody(body: Entity): T

  /**
   * Creates message with supplied attributes.
   *
   * @param attributes message attributes
   *
   * @return new message
   *
   * @note All previous attributes are removed.
   */
  def withAttributes(attributes: Map[String, Any]): T

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
  def withAttributes(one: (String, Any), more: (String, Any)*): T =
    withAttributes((one +: more).toMap)

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
      case false =>
        withAttributes {
          attributes.filterNot(a => names.contains(a._1))
        }
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

  /**
   * Creates message with supplied attribute.
   *
   * @param attribute name/value pair
   *
   * @return new message
   *
   * @note If attribute already exists, its value is replaced.
   */
  def withAttribute(attribute: (String, Any)): T =
    withAttributes(attributes + attribute)
}
