/*
 * Copyright 2023 Carlos Conyers
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

import Values.notNull

/**
 * Defines URI builder.
 *
 * @constructor Creates builder initialized to supplied URI.
 */
class UriBuilder(uri: Uri): //
  /** Creates builder. */
  def this() = this(Uri(""))

  private var schemeOption: Option[String]    = uri.schemeOption
  private var authorityOption: Option[String] = uri.authorityOption
  private var pathValue: String               = uri.path
  private var queryValue: QueryString         = uri.query
  private var fragmentOption: Option[String]  = uri.fragmentOption

  /** Sets scheme. */
  def scheme(value: String): this.type =
    scheme(Some(value))

  /** Sets scheme. */
  def scheme(value: Option[String]): this.type =
    schemeOption = value.map(_.trim)
    this

  /** Sets authority. */
  def authority(value: String): this.type =
    authority(Some(value))

  /** Sets authority. */
  def authority(value: Option[String]): this.type =
    authorityOption = value.map(_.trim)
    this

  /** Sets authority. */
  def authority(host: String, port: Int): this.type =
    authority(host, Some(port))

  /** Sets authority. */
  def authority(host: String, port: Option[Int]): this.type =
    host.trim match
      case host => authority(port.map(n => s"$host:$n").getOrElse(host))

  /** Sets path. */
  def path(value: String): this.type =
    pathValue = value.trim
    this

  /** Sets query. */
  def query(value: QueryString): this.type =
    queryValue = notNull(value)
    this

  /** Sets fragment. */
  def fragment(value: String): this.type =
    fragment(Some(value))

  /** Sets fragment. */
  def fragment(value: Option[String]): this.type =
    fragmentOption = value.map(_.trim)
    this

  /** Clears previously set values. */
  def clear(): this.type =
    schemeOption    = None
    authorityOption = None
    pathValue       = ""
    queryValue      = QueryString.empty
    fragmentOption  = None
    this

  /** Creates URI from currently set values. */
  def toUri: Uri =
    val uri = StringBuilder()

    schemeOption.foreach { scheme => uri.append(scheme).append(":") }
    authorityOption.foreach { authority => uri.append("//").append(authority) }

    if pathValue.nonEmpty then
      uri.append('/').append(pathValue.dropWhile(_ == '/'))

    if ! queryValue.isEmpty then
      uri.append('?').append(queryValue)

    fragmentOption.foreach { fragment => uri.append('#').append(fragment) }

    Uri(uri.toString)
