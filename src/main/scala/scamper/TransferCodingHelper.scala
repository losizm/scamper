package scamper

import Grammar._

private object TransferCodingHelper {
  private val syntax = """\s*([\w!#$%&'*+.^`|~-]+)(\s*(?:;.*)?)""".r

  def Name(name: String): String =
    Token(name).map(_.toLowerCase) getOrElse {
      throw new IllegalArgumentException(s"Invalid name: $name")
    }

  def Params(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParamName(name) -> ParamValue(value) }

  def ParamName(name: String): String =
    Token(name) getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter name: $name")
    }

  def ParamValue(value: String): String =
    Token(value) orElse QuotableString(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def ParseTransferCoding(coding: String): (String, Map[String, String]) =
    coding match {
      case syntax(name, params) => (name.trim, ParseParams(params))
      case _ => throw new IllegalArgumentException(s"Malformed transfer coding: $coding")
    }

  def ParseParams(params: String): Map[String, String] =
    StandardParams.parse(params)

  def FormatParams(params: Map[String, String]): String =
    StandardParams.format(params)
}

