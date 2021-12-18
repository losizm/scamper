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
package types

import Grammar.*

private object StandardParams:
  private val TokenParam = """\s*;\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val QuotedParam = """\s*;\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r

  def parse(params: String): Map[String, String] =
    parseParams(params, Map.empty)

  def format(params: Map[String, String]): String =
    params.map { case (name, value) => s"; $name=${formatParamValue(value)}" }.mkString

  @annotation.tailrec
  private def parseParams(params: String, collected: Map[String, String]): Map[String, String] =
    findPrefixParam(params) match
      case None =>
        if params.matches("\\s*") then collected
        else throw IllegalArgumentException(s"Malformed parameters: $params")
      case Some((name, value, suffix)) => parseParams(suffix, collected + (name -> value))

  private def findPrefixParam(text: String): Option[(String, String, String)] =
    TokenParam.findPrefixMatchOf(text).orElse(QuotedParam.findPrefixMatchOf(text)).map { m =>
      (m.group(1), m.group(2), m.after.toString)
    }

  private def formatParamValue(value: String): String = Token(value).getOrElse(s"""\"$value\"""")
