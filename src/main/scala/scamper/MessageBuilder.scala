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
  def withHeaders(headers: Header*): T

  /**
   * Creates message with additional headers.
   *
   * @param headers additional message headers
   *
   * @return new message
   */
  def addHeaders(headers: Header*): T

  /**
   * Creates message excluding headers with given field names.
   *
   * @param names header field names
   *
   * @return new message
   */
  def removeHeaders(names: String*): T

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
}
