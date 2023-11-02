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

import scamper.http.types.MediaType

/** Provides standardized access to Content-Type header. */
given toContentType[T <: HttpMessage]: Conversion[T, ContentType[T]] = ContentType(_)

/** Provides standardized access to Content-Type header. */
class ContentType[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Content-Type header. */
  def hasContentType: Boolean =
    message.hasHeader("Content-Type")

  /**
   * Gets Content-Type header value.
   *
   * @throws HeaderNotFound if Content-Type is not present
   */
  def contentType: MediaType =
    contentTypeOption.getOrElse(throw HeaderNotFound("Content-Type"))

  /** Gets Content-Type header value if present. */
  def contentTypeOption: Option[MediaType] =
    message.getHeaderValue("Content-Type").map(MediaType.apply)

  /** Creates new message with Content-Type header set to supplied value. */
  def setContentType(value: MediaType): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Content-Type", value.toString))

  /** Creates new message with Content-Type header removed. */
  def contentTypeRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Content-Type")
