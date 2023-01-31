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

import java.time.Instant

/** Provides standardized access to Date header. */
implicit class Date[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Date header. */
  def hasDate: Boolean =
    message.hasHeader("Date")

  /**
   * Gets Date header value.
   *
   * @throws HeaderNotFound if Date is not present
   */
  def date: Instant =
    dateOption.getOrElse(throw HeaderNotFound("Date"))

  /** Gets Date header value if present. */
  def dateOption: Option[Instant] =
    message.getHeader("Date").map(_.dateValue)

  /** Creates new message with Date header set to supplied value. */
  def setDate(value: Instant = Instant.now()): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Date", value))

  /** Creates new message with Date header removed. */
  def dateRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Date")
