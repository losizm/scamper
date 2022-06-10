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

import scamper.http.types.ViaType

/** Provides standardized access to Via header. */
implicit class Via[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Via header. */
  def hasVia: Boolean =
    message.hasHeader("Via")

  /**
   * Gets Via header values.
   *
   * @return header values or empty sequence if Via is not present
   */
  def via: Seq[ViaType] =
    getVia.getOrElse(Nil)

  /** Gets Via header values if present. */
  def getVia: Option[Seq[ViaType]] =
    message.getHeaderValue("Via").map(ViaType.parseAll)

  /** Creates new message with Via header set to supplied values. */
  def setVia(values: Seq[ViaType]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Via", values.mkString(", ")))

  /** Creates new message with Via header set to supplied values. */
  def setVia(one: ViaType, more: ViaType*): T =
    setVia(one +: more)

  /** Creates new message with Via header removed. */
  def removeVia: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Via")