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

/** Provides standardized access to Content-Location header. */
implicit class ContentLocation[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Content-Location header. */
  def hasContentLocation: Boolean =
    message.hasHeader("Content-Location")

  /**
   * Gets Content-Location header value.
   *
   * @throws HeaderNotFound if Content-Location is not present
   */
  def contentLocation: Uri =
    contentLocationOption.getOrElse(throw HeaderNotFound("Content-Location"))

  /** Gets Content-Location header value if present. */
  def contentLocationOption: Option[Uri] =
    message.getHeaderValue("Content-Location").map(Uri(_))

  /** Creates new message with Content-Location header set to supplied value. */
  def setContentLocation(value: Uri): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Content-Location", value.toString))

  /** Creates new message with Content-Location header removed. */
  def contentLocationRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Content-Location")
