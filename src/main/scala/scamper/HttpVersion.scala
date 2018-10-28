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

import scala.util.Try

/** HTTP version */
trait HttpVersion {
  /** Version major */
  def major: Int

  /** Version minor */
  def minor: Int

  /** Returns formatted HTTP version. */
  override val toString: String = s"$major.$minor"
}

/** HttpVersion factory */
object HttpVersion {
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
    HttpVersionImpl(major, minor)

  /** Destructures HttpVersion. */
  def unapply(version: HttpVersion): Option[(Int, Int)] =
    Some((version.major, version.minor))
}

private case class HttpVersionImpl(major: Int, minor: Int) extends HttpVersion
