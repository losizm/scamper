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

import scala.util.Try

private case class CookieAttributes(domain: Option[String] = None, path: Option[String] = None, expires: Option[OffsetDateTime] = None,
    maxAge: Option[Long] = None, secure: Boolean = false, httpOnly: Boolean = false)

private object CookieAttributes {
  def apply(attribs: String): CookieAttributes =
    attribs.split(";").map(_.split("=", 2).map(_.trim.toLowerCase)).foldRight(CookieAttributes())(append)

  private def append(attrib: Array[String], attribs: CookieAttributes): CookieAttributes =
    attrib match {
      case Array(name, value) if name == "domain"   => attribs.copy(domain = Some(value))
      case Array(name, value) if name == "path"     => attribs.copy(path = Some(value))
      case Array(name, value) if name == "expires"  => attribs.copy(expires = toExpires(value))
      case Array(name, value) if name == "max-age"  => attribs.copy(maxAge = toMaxAge(value))
      case Array(name)        if name == "secure"   => attribs.copy(secure = true)
      case Array(name)        if name == "httponly" => attribs.copy(httpOnly = true)
      case _ => attribs
    }

  private def toMaxAge(value: String): Option[Long] =
    Try(value.trim.toLong).toOption

  private def toExpires(value: String): Option[OffsetDateTime] =
    Try(DateValue.parse(value.trim)).toOption
}
