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

import Grammar._

private object AuthParams {
  private val TokenParam = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val QuotedParam = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r

  def parse(params: String): Map[String, String] =
    ListParser(params).map {
      case TokenParam(name, value)  => name -> value
      case QuotedParam(name, value) => name -> value
      case param => throw new IllegalArgumentException(s"Malformed auth parameters: $param")
    }.toMap

  def format(params: Map[String, String]): String =
    if (params.isEmpty) ""
    else params.map { case (name, value) => s"$name=${formatParamValue(value)}" }.mkString(" ", ", ", "")

  private def formatParamValue(value: String): String = Token(value).getOrElse('"' + value + '"')
}

