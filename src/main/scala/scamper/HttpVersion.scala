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

import scala.util.Try

/** HTTP version */
trait HttpVersion {
  /** Gets version major. */
  def major: Int

  /** Gets version minor. */
  def minor: Int

  /** Returns formatted HTTP version. */
  override val toString: String = s"$major.$minor"
}

/** Provides factory methods for `HttpVersion`. */
object HttpVersion {
  private val `HTTP/1.0` = HttpVersionImpl(1, 0)
  private val `HTTP/1.1` = HttpVersionImpl(1, 1)
  private val `HTTP/2.0` = HttpVersionImpl(2, 0)

  private val syntax = """(\d+)(?:\.(\d+))?""".r

  /** Parses formatted HTTP version. */
  def parse(version: String): HttpVersion =
    Try {
      version match {
        case syntax(major, null)  => HttpVersionImpl(major.toInt, 0)
        case syntax(major, minor) => HttpVersionImpl(major.toInt, minor.toInt)
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Invalid HTTP version: $version")
    }

  /** Creates HttpVersion with supplied major and minor. */
  def apply(major: Int, minor: Int): HttpVersion =
    (major, minor) match {
      case (1, 1) => `HTTP/1.1`
      case (2, 0) => `HTTP/2.0`
      case (1, 0) => `HTTP/1.0`
      case (_, _) => HttpVersionImpl(major, minor)
    }

  /** Destructures HttpVersion. */
  def unapply(version: HttpVersion): Option[(Int, Int)] =
    Some((version.major, version.minor))
}

private case class HttpVersionImpl(major: Int, minor: Int) extends HttpVersion
