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

private object MediaTypeHelper:
  private val syntax = """\s*([\w!#$%&'*+.^`|~-]+)/([\w!#$%&'*+.^`|~-]+)(\s*(?:;.*)?)""".r

  def TypeName(typeName: String): String =
    Token(typeName) getOrElse {
      throw IllegalArgumentException(s"Invalid type name: $typeName")
    }

  def SubtypeName(subtypeName: String): String =
    Token(subtypeName) getOrElse {
      throw IllegalArgumentException(s"Invalid subtype name: $subtypeName")
    }

  def Params(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParamName(name) -> ParamValue(value) }

  def ParamName(name: String): String =
    Token(name) getOrElse {
      throw IllegalArgumentException(s"Invalid parameter name: $name")
    }

  def ParamValue(value: String): String =
    Token(value) orElse QuotableString(value) getOrElse {
      throw IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def ParseMediaType(mediaType: String): (String, String, Map[String, String]) =
    mediaType match
      case syntax(typeName, subtypeName, params) => (typeName, subtypeName, ParseParams(params))
      case _ => throw IllegalArgumentException(s"Malformed media type: $mediaType")

  def ParseParams(params: String): Map[String, String] =
    StandardParams.parse(params)

  def FormatParams(params: Map[String, String]): String =
    StandardParams.format(params)
