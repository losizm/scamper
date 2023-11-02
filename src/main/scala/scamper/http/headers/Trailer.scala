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

/** Provides standardized access to Trailer header. */
given toTrailer[T <: HttpMessage]: Conversion[T, Trailer[T]] = Trailer(_)

/** Provides standardized access to Trailer header. */
class Trailer[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Trailer header. */
  def hasTrailer: Boolean =
    message.hasHeader("Trailer")

  /**
   * Gets Trailer header values.
   *
   * @return header values or empty sequence if Trailer is not present
   */
  def trailer: Seq[String] =
    trailerOption.getOrElse(Nil)

  /** Gets Trailer header values if present. */
  def trailerOption: Option[Seq[String]] =
    message.getHeaderValue("Trailer").map(ListParser.apply)

  /** Creates new message with Trailer header set to supplied values. */
  def setTrailer(values: Seq[String]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Trailer", values.mkString(", ")))

  /** Creates new message with Trailer header set to supplied values. */
  def setTrailer(one: String, more: String*): T =
    setTrailer(one +: more)

  /** Creates new message with Trailer header removed. */
  def trailerRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Trailer")
