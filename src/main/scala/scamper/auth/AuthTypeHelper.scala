/*
 * Copyright 2017-2020 Carlos Conyers
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
package scamper.auth

import scamper.{ CaseInsensitiveKeyMap, ListParser }
import scamper.Grammar.{ QuotableString, Token => StandardToken, Token68 }

private object AuthTypeHelper {
  private val syntax = """\s*([\w!#$%&'*+.^`|~-]+)(?:\s+(?:([\w!#$%&'*+.^`|~-]+=*)|([\w.~+/-]+\s*=\s*[^ =].*)))?\s*""".r
  private val StartSyntax = """((?:[\w!#$%&'*+.^`|~-]+)(?:\s+(?:.+))?)""".r

  def Scheme(value: String): String =
    StandardToken(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid auth scheme: $value")
    }

  def Token(value: String): String =
    Token68(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid auth token: $value")
    }

  def Params(params: Seq[(String, String)]): Map[String, String] =
    new CaseInsensitiveKeyMap(params.map {
      case (name, value) => ParamName(name) -> ParamValue(value)
    })

  def ParamName(name: String): String =
    StandardToken(name) getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter name: $name")
    }

  def ParamValue(value: String): String =
    StandardToken(value) orElse QuotableString(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def ParseAuthType(auth: String): (String, Option[String], Map[String, String]) =
    auth match {
      case syntax(scheme, null, null)   => (scheme, None, Map.empty)
      case syntax(scheme, token, null)  => (scheme, Some(token), Map.empty)
      case syntax(scheme, null, params) => (scheme, None, AuthParams.parse(params))
      case _ => throw new IllegalArgumentException(s"Malformed auth type: $auth")
    }

  def SplitAuthTypes(auths: String): Seq[String] =
    ListParser(auths).foldLeft(Seq.empty[String]) {
      case (xs, StartSyntax(x)) => xs :+ x
      case (head :+ tail, x) => head :+ (tail + ", " + x)
      case (Nil, x) => Seq(x)
    }

  def ParseParams(params: String): Map[String, String] =
    AuthParams.parse(params)

  def FormatParams(params: Map[String, String]): String =
    AuthParams.format(params)
}
