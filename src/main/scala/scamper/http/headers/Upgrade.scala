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

import scamper.http.types.Protocol

/** Provides standardized access to Upgrade header. */
implicit class Upgrade[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Upgrade header. */
  def hasUpgrade: Boolean =
    message.hasHeader("Upgrade")

  /**
   * Gets Upgrade header values.
   *
   * @return header values or empty sequence if Upgrade is not present
   */
  def upgrade: Seq[Protocol] =
    upgradeOption.getOrElse(Nil)

  /** Gets Upgrade header values if present. */
  def upgradeOption: Option[Seq[Protocol]] =
    message.getHeaderValue("Upgrade")
      .map(ListParser.apply)
      .map(_.map(Protocol.parse))

  /** Creates new message with Upgrade header set to supplied values. */
  def setUpgrade(values: Seq[Protocol]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Upgrade", values.mkString(", ")))

  /** Creates new message with Upgrade header set to supplied values. */
  def setUpgrade(one: Protocol, more: Protocol*): T =
    setUpgrade(one +: more)

  /** Creates new message with Upgrade header removed. */
  def upgradeRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Upgrade")
