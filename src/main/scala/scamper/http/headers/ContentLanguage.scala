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

import scamper.http.types.LanguageTag

/** Provides standardized access to Content-Language header. */
given toContentLanguage[T <: HttpMessage]: Conversion[T, ContentLanguage[T]] = ContentLanguage(_)

/** Provides standardized access to Content-Language header. */
class ContentLanguage[T <: HttpMessage](message: T) extends AnyVal:
  /** Tests for Content-Language header. */
  def hasContentLanguage: Boolean =
    message.hasHeader("Content-Language")

  /**
   * Gets Content-Language header values.
   *
   * @return header values or empty sequence if Content-Language is not present
   */
  def contentLanguage: Seq[LanguageTag] =
    contentLanguageOption.getOrElse(Nil)

  /** Gets Content-Language header values if present. */
  def contentLanguageOption: Option[Seq[LanguageTag]] =
    message.getHeaderValue("Content-Language")
      .map(ListParser.apply)
      .map(_.map(LanguageTag.parse))

  /** Creates new message with Content-Language header set to supplied values. */
  def setContentLanguage(values: Seq[LanguageTag]): T =
    message.asInstanceOf[MessageBuilder[T]].putHeaders(Header("Content-Language", values.mkString(", ")))

  /** Creates new message with Content-Language header set to supplied values. */
  def setContentLanguage(one: LanguageTag, more: LanguageTag*): T =
    setContentLanguage(one +: more)

  /** Creates new message with Content-Language header removed. */
  def contentLanguageRemoved: T =
    message.asInstanceOf[MessageBuilder[T]].removeHeaders("Content-Language")
