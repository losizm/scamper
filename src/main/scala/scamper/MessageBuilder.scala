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
trait MessageBuilder[T <: HttpMessage] {
  /**
   * Creates message with supplied start line.
   *
   * @param startLine message start line
   *
   * @return new message
   */
  def withStartLine(line: T#LineType): T

  /**
   * Creates message with supplied headers. All previous headers are removed.
   *
   * @param headers message headers
   *
   * @return new message
   */
  def withHeaders(headers: Seq[Header]): T

  /**
   * Creates message with supplied headers. All previous headers are removed.
   *
   * @param one message header
   * @param more additional message headers
   *
   * @return new message
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
  def addHeaders(headers: Seq[Header]): T

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
   * Creates message excluding headers with given field names.
   *
   * @param names header field names
   *
   * @return new message
   */
  def removeHeaders(names: Seq[String]): T

  /**
   * Creates message excluding headers with given field names.
   *
   * @param one header field name
   * @param more additional header field names
   *
   * @return new message
   */
  def removeHeaders(one: String, more: String*): T =
    removeHeaders(one +: more)

  /**
   * Creates message with supplied header. All previous headers with same field
   * name are removed.
   *
   * @param header message header
   *
   * @return new message
   */
  def withHeader(header: Header): T

  /**
   * Creates message with supplied body.
   *
   * @param body message body
   *
   * @return new message
   */
  def withBody(body: Entity): T

  /**
   * Creates message with supplied attributes. All previous attributes are
   * removed.
   *
   * @param attributes message attributes
   *
   * @return new message
   */
  def withAttributes(attributes: Map[String, Any]): T

  /**
   * Creates message with supplied attributes. All previous attributes are
   * removed.
   *
   * @param one message attribute
   * @param more additional message attributes
   *
   * @return new message
   */
  def withAttributes(one: (String, Any), more: (String, Any)*): T =
    withAttributes((one +: more).toMap)

  /**
   * Creates message excluding attribute with given names.
   *
   * @param name attribute names
   *
   * @return new message
   */
  def removeAttributes(names: Seq[String]): T

  /**
   * Creates message excluding attribute with given name.
   *
   * @param one attribute name
   * @param more additional attribute names
   *
   * @return new message
   */
  def removeAttributes(one: String, names: String*): T =
    removeAttributes(one +: names)

  /**
   * Creates message with supplied attribute, replacing existing value, if any.
   *
   * @param attribute name/value pair
   *
   * @return new message
   */
  def withAttribute(attribute: (String, Any)): T
}
