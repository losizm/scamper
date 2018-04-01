/*
 * Copyright 2018 Carlos Conyers
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

import java.time.OffsetDateTime

import HeaderHelper._

/** HTTP header */
trait Header {
  /** Header key */
  def key: String

  /** Header value */
  def value: String

  /** Gets header value as OffsetDateTime. */
  def dateValue: OffsetDateTime =
    DateValue.parse(value)

  /** Gets header value as Long. */
  def longValue: Long = value.toLong

  /** Returns formatted HTTP header. */
  override lazy val toString: String = s"$key: $value"
}

/** Header factory */
object Header {
  /** Creates Header using supplied key and value. */
  def apply(key: String, value: String): Header =
    HeaderImpl(Key(key), Value(value))

  /** Creates Header using supplied key and value. */
  def apply(key: String, value: Long): Header =
    apply(key, value.toString)

  /** Creates Header using supplied key and value. */
  def apply(key: String, value: OffsetDateTime): Header =
    apply(key, DateValue.format(value))

  /** Parses formatted header. */
  def apply(header: String): Header =
    header.split(":", 2) match {
      case Array(key, value) => apply(key.trim, value.trim)
      case _ => throw new IllegalArgumentException(s"Malformed header: $header")
    }

  /** Destructures Header to key-value pair. */
  def unapply(header: Header): Option[(String, String)] =
    Some(header.key -> header.value)
}

private case class HeaderImpl(key: String, value: String) extends Header

