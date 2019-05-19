/*
 * Copyright 2019 Carlos Conyers
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

/** HTTP header */
trait Header {
  /** Gets header name. */
  def name: String

  /** Gets header value. */
  def value: String

  /** Gets header value as `Instant`. */
  def dateValue: Instant = DateValue.parse(value)

  /** Gets header value as Long. */
  def longValue: Long = value.toLong

  /** Returns formatted header. */
  override lazy val toString: String = s"$name: $value"
}

/** Provided for factory for `Header`. */
object Header {
  /** Creates `Header` using supplied name and value. */
  def apply(name: String, value: String): Header =
    HeaderImpl(Name(name), Value(value))

  /** Creates `Header` using supplied name and value. */
  def apply(name: String, value: Long): Header =
    apply(name, value.toString)

  /** Creates `Header` using supplied name and value. */
  def apply(name: String, value: Instant): Header =
    apply(name, DateValue.format(value))

  /** Parses formatted header. */
  def parse(header: String): Header =
    header.split(":", 2) match {
      case Array(name, value) => apply(name.trim, value.trim)
      case _ => throw new IllegalArgumentException(s"Malformed header: $header")
    }

  /** Destructures `Header`. */
  def unapply(header: Header): Option[(String, String)] =
    Some(header.name -> header.value)
}

private case class HeaderImpl(name: String, value: String) extends Header
