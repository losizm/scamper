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

private object DispositionTypeHelper:
  private val syntax = """\s*([\w!#$%&'*+.^`|~-]+)(\s*(?:;.*)?)""".r

  def Name(name: String): String =
    Token(name).getOrElse {
      throw IllegalArgumentException(s"Invalid name: $name")
    }.toLowerCase

  def Params(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParamName(name) -> ParamValue(value) }

  def ParamName(name: String): String =
    Token(name).getOrElse {
      throw IllegalArgumentException(s"Invalid parameter name: $name")
    }.toLowerCase

  def ParamValue(value: String): String =
    Token(value) orElse QuotableString(value) getOrElse {
      throw IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def ParseContentDisposition(coding: String): (String, Map[String, String]) =
    coding match
      case syntax(name, params) => (name.trim, ParseParams(params))
      case _ => throw IllegalArgumentException(s"Malformed transfer coding: $coding")

  def ParseParams(params: String): Map[String, String] =
    StandardParams.parse(params)

  def FormatParams(params: Map[String, String]): String =
    StandardParams.format(params)
