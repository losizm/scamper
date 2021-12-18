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

import scamper.http.types.KeepAliveParameters

/** Provides standardized access to Keep-Alive header. */
implicit class KeepAlive[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Keep-Alive header. */
  def hasKeepAlive: Boolean =
    message.hasHeader("Keep-Alive")

  /**
   * Gets Keep-Alive header value.
   *
   * @throws HeaderNotFound if Keep-Alive is not present
   */
  def keepAlive: KeepAliveParameters =
    getKeepAlive.getOrElse(throw HeaderNotFound("Keep-Alive"))

  /** Gets Keep-Alive header value if present. */
  def getKeepAlive: Option[KeepAliveParameters] =
    message.getHeaderValue("Keep-Alive").map(KeepAliveParameters.parse)

  /** Creates new message with Keep-Alive header set to supplied value. */
  def setKeepAlive(value: KeepAliveParameters): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Keep-Alive", value.toString))

  /** Creates new message with Keep-Alive header removed. */
  def removeKeepAlive: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Keep-Alive")
