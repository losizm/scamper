package scamper.types

import scamper.Grammar.{ QuotableString, Token => StandardToken, Token68 }

private object AuthTypeHelper {
  private val syntax = """\s*([\w!#$%&'*+.^`|~-]+)(?:\s+(?:([\w!#$%&'*+.^`|~-]+=*)|([\w.~+/-]+\s*=\s*[^ =].*)))?\s*""".r

  def Scheme(value: String): String =
    StandardToken(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid auth scheme: $value")
    }

  def Token(value: String): String =
    Token68(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid auth token: $value")
    }

  def Params(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParamName(name) -> ParamValue(value) }

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

  def ParseParams(params: String): Map[String, String] =
    AuthParams.parse(params)

  def FormatParams(params: Map[String, String]): String =
    AuthParams.format(params)
}

