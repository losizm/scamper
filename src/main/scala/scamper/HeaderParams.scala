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

import Grammar._

private object HeaderParams {
  private val NoValue     = """\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val TokenValue  = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val QuotedValue = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r
  private val BadValue    = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*(.+)\s*""".r

  def parse(params: String): Map[String, Option[String]] =
    ListParser(params, semicolon = true).map {
      case NoValue(name)            => name -> None
      case TokenValue(name, value)  => name -> Some(value)
      case QuotedValue(name, value) => name -> Some(value)
      case BadValue(name, value)    => name -> Some(value)
      case param => throw new IllegalArgumentException(s"Malformed parameters: $param")
    }.toMap

  def format(params: Map[String, Option[String]]): String =
    if (params.isEmpty) ""
    else
      params.map {
        case (name, Some(value)) => s"; $name=${formatParamValue(value)}"
        case (name, None)        => s"; $name"
      }.mkString

  private def formatParamValue(value: String): String = Token(value).getOrElse(s"""\"$value\"""")
}
