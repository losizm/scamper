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

import scala.util.Try

/** Defines HTTP version. */
sealed trait HttpVersion:
  /** Gets major version. */
  def major: Int

  /** Gets minor version. */
  def minor: Int

/** Provides factory for `HttpVersion`. */
object HttpVersion:
  private val `HTTP/1.0` = HttpVersionImpl(1, 0)
  private val `HTTP/1.1` = HttpVersionImpl(1, 1)
  private val `HTTP/2.0` = HttpVersionImpl(2, 0)

  private val syntax = """HTTP/(\d+)(?:\.(\d+))?""".r

  /** Parses formatted HTTP version. */
  def apply(version: String): HttpVersion =
    Try {
      version match
        case syntax(major, null)  => HttpVersionImpl(major.toInt, 0)
        case syntax(major, minor) => HttpVersionImpl(major.toInt, minor.toInt)
    } getOrElse {
      throw IllegalArgumentException(s"Invalid HTTP version: $version")
    }

  /** Creates HTTP version with supplied major and minor. */
  def apply(major: Int, minor: Int): HttpVersion =
    (major, minor) match
      case (1, 1) => `HTTP/1.1`
      case (2, 0) => `HTTP/2.0`
      case (1, 0) => `HTTP/1.0`
      case (_, _) => HttpVersionImpl(major, minor)

private case class HttpVersionImpl(major: Int, minor: Int) extends HttpVersion:
  override val toString = s"HTTP/$major.$minor"
