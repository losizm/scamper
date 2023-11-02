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

import scamper.http.types.CacheDirective

/** Provides standardized access to Cache-Control header. */
given toCacheControl[T <: HttpMessage]: Conversion[T, CacheControl[T]] = CacheControl(_)

/** Provides standardized access to Cache-Control header. */
class CacheControl[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Cache-Control header. */
  def hasCacheControl: Boolean =
    message.hasHeader("Cache-Control")

  /**
   * Gets Cache-Control header values.
   *
   * @return header values or empty sequence if Cache-Control is not present
   */
  def cacheControl: Seq[CacheDirective] =
    cacheControlOption.getOrElse(Nil)

  /** Gets Cache-Control header values if present. */
  def cacheControlOption: Option[Seq[CacheDirective]] =
    message.getHeaderValue("Cache-Control").map(CacheDirective.parseAll)

  /** Creates new message with Cache-Control header set to supplied values. */
  def setCacheControl(values: Seq[CacheDirective]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Cache-Control", values.mkString(", ")))

  /** Creates new message with Cache-Control header set to supplied values. */
  def setCacheControl(one: CacheDirective, more: CacheDirective*): T =
    setCacheControl(one +: more)

  /** Creates new message with Cache-Control header removed. */
  def cacheControlRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Cache-Control")
