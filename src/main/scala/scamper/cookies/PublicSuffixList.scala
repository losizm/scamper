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
package scamper.cookies

import java.net.URL

import scala.io.Source
import scala.util.Try

import scamper.RuntimeProperties.cookies.*

/** Checks domains for public suffix. */
private object PublicSuffixList:
  private lazy val list: Seq[String] =
    Try(getRemoteList())
      .orElse(Try(getLocalList()))
      .getOrElse(Nil)

  private lazy val includes: Seq[String] =
    list.filterNot(_.startsWith("!"))
      .map(toRegex)

  private lazy val excludes: Seq[String] =
    list.filter(_.startsWith("!"))
      .map(_.drop(1))
      .map(toRegex)

  /**
   * Checks whether domain is public suffix.
   *
   * @param domain canonicalized domain
   *
   * @return `true` if domain is public suffix; `false` otherwise
   */
  def check(domain: String): Boolean =
    includes.exists(domain.matches) && !excludes.exists(domain.matches)

  private def getRemoteList(): Seq[String] =
    getRemotePublicSuffixList match
      case true  => getLines(URL(publicSuffixListUrl))
      case false => throw UnsupportedOperationException()

  private def getLocalList(): Seq[String] =
    val url = getClass.getResource("public_suffix_list.dat")
    getLines(url)

  private def getLines(url: URL): Seq[String] =
    Source.fromURL(url)
      .getLines()
      .map(line => line.split("\\s+", 2).head)
      .filterNot(line => line.isEmpty || line.startsWith("//"))
      .toSeq

  private def toRegex(suffix: String): String =
    "(?i)" + suffix.replaceAll("""\.""", """\\.""")
        .replaceAll("""\*""", """[^.]+""")
