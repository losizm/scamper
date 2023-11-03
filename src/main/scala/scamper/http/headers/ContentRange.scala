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

import scamper.http.types.ByteContentRange

/** Adds standardized access to Content-Range header. */
given toContentRange[T <: HttpMessage]: Conversion[T, ContentRange[T]] = ContentRange(_)

/** Adds standardized access to Content-Range header. */
class ContentRange[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Content-Range header. */
  def hasContentRange: Boolean =
    message.hasHeader("Content-Range")

  /**
   * Gets Content-Range header value.
   *
   * @throws HeaderNotFound if Content-Range is not present
   */
  def contentRange: ByteContentRange =
    contentRangeOption.getOrElse(throw HeaderNotFound("Content-Range"))

  /** Gets Content-Range header value if present. */
  def contentRangeOption: Option[ByteContentRange] =
    message.getHeaderValue("Content-Range").map(ByteContentRange.parse)

  /** Creates new message with Content-Range header set to supplied value. */
  def setContentRange(value: ByteContentRange): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Content-Range", value.toString))

  /** Creates new message with Content-Range header removed. */
  def contentRangeRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Content-Range")
