/*
 * Copyright 2021 Carlos Conyers
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
package headers

import scamper.http.types.ContentCoding

/** Adds standardized access to Content-Encoding header. */
given toContentEncoding[T <: HttpMessage]: Conversion[T, ContentEncoding[T]] = ContentEncoding(_)

/** Adds standardized access to Content-Encoding header. */
class ContentEncoding[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Content-Encoding header. */
  def hasContentEncoding: Boolean =
    message.hasHeader("Content-Encoding")

  /**
   * Gets Content-Encoding header values.
   *
   * @return header values or empty sequence if Content-Encoding is not present
   */
  def contentEncoding: Seq[ContentCoding] =
    contentEncodingOption.getOrElse(Nil)

  /** Gets Content-Encoding header values if present. */
  def contentEncodingOption: Option[Seq[ContentCoding]] =
    message.getHeaderValue("Content-Encoding")
      .map(ListParser.apply)
      .map(_.map(ContentCoding.apply))

  /** Creates new message with Content-Encoding header set to supplied values. */
  def setContentEncoding(values: Seq[ContentCoding]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Content-Encoding", values.mkString(", ")))

  /** Creates new message with Content-Encoding header set to supplied values. */
  def setContentEncoding(one: ContentCoding, more: ContentCoding*): T =
    setContentEncoding(one +: more)

  /** Creates new message with Content-Encoding header removed. */
  def contentEncodingRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Content-Encoding")
