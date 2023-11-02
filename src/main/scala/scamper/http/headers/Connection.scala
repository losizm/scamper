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

/** Provides standardized access to Connection header. */
given toConnection[T <: HttpMessage]: Conversion[T, Connection[T]] = Connection(_)

/** Provides standardized access to Connection header. */
class Connection[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Connection header. */
  def hasConnection: Boolean =
    message.hasHeader("Connection")

  /**
   * Gets Connection header values.
   *
   * @return header values or empty sequence if Connection is not present
   */
  def connection: Seq[String] =
    connectionOption.getOrElse(Nil)

  /** Gets Connection header values if present. */
  def connectionOption: Option[Seq[String]] =
    message.getHeaderValue("Connection").map(ListParser.apply)

  /** Creates new message with Connection header set to supplied values. */
  def setConnection(values: Seq[String]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Connection", values.mkString(", ")))

  /** Creates new message with Connection header set to supplied values. */
  def setConnection(one: String, more: String*): T =
    setConnection(one +: more)

  /** Creates new message with Connection header removed. */
  def connectionRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Connection")
