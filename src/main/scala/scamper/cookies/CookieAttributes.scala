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
package scamper.cookies

import java.time.Instant

import scala.util.Try

import scamper.DateValue

private case class CookieAttributes(
  domain: Option[String] = None,
  path: Option[String] = None,
  expires: Option[Instant] = None,
  maxAge: Option[Long] = None,
  secure: Boolean = false,
  httpOnly: Boolean = false
)

private object CookieAttributes {
  def parse(attrs: String): CookieAttributes =
    attrs.split(";")
      .map(_.split("=", 2).map(_.trim.toLowerCase))
      .foldRight(CookieAttributes())(append)

  private def append(attr: Array[String], attrs: CookieAttributes): CookieAttributes =
    attr match {
      case Array("domain", value)  => attrs.copy(domain = Some(value))
      case Array("path", value)    => attrs.copy(path = Some(value))
      case Array("expires", value) => attrs.copy(expires = toExpires(value))
      case Array("max-age", value) => attrs.copy(maxAge = toMaxAge(value))
      case Array("secure")         => attrs.copy(secure = true)
      case Array("httponly")       => attrs.copy(httpOnly = true)
      case _                       => attrs
    }

  private def toMaxAge(value: String): Option[Long] =
    Try(value.toLong).toOption

  private def toExpires(value: String): Option[Instant] =
    Try(DateValue.parse(value)).toOption
}
