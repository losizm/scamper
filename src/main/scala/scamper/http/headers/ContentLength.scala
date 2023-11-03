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

/** Adds standardized access to Content-Length header. */
given toContentLength[T <: HttpMessage]: Conversion[T, ContentLength[T]] = ContentLength(_)

/** Adds standardized access to Content-Length header. */
class ContentLength[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Content-Length header. */
  def hasContentLength: Boolean =
    message.hasHeader("Content-Length")

  /**
   * Gets Content-Length header value.
   *
   * @throws HeaderNotFound if Content-Length is not present
   */
  def contentLength: Long =
    contentLengthOption.getOrElse(throw HeaderNotFound("Content-Length"))

  /** Gets Content-Length header value if present. */
  def contentLengthOption: Option[Long] =
    message.getHeader("Content-Length").map(_.longValue)

  /** Creates new message with Content-Length header set to supplied value. */
  def setContentLength(value: Long): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Content-Length", value))

  /** Creates new message with Content-Length header removed. */
  def contentLengthRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Content-Length")
