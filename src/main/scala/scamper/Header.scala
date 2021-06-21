/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.time.Instant

import HeaderHelper._

/** Defines HTTP header. */
sealed trait Header {
  /** Gets header name. */
  def name: String

  /** Gets header value. */
  def value: String

  /** Gets header value as `Instant`. */
  def dateValue: Instant = DateValue.parse(value)

  /** Gets header value as `Long`. */
  def longValue: Long = value.toLong
}

/** Provides factory for `Header`. */
object Header {
  /** Creates header using supplied name and value. */
  def apply(name: String, value: String): Header =
    HeaderImpl(Name(name), Value(value))

  /** Creates header using supplied name and value. */
  def apply(name: String, value: Long): Header =
    apply(name, value.toString)

  /** Creates header using supplied name and value. */
  def apply(name: String, value: Instant): Header =
    apply(name, DateValue.format(value))

  /** Parses formatted header. */
  def apply(header: String): Header =
    header.split(":", 2) match {
      case Array(name, value) => apply(name.trim, value.trim)
      case _ => throw new IllegalArgumentException(s"Malformed header: $header")
    }
}

private case class HeaderImpl(name: String, value: String) extends Header {
  override lazy val toString = s"$name: $value"
}
