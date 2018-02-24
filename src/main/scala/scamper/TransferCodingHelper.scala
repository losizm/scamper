package scamper

import Grammar._

private object TransferCodingHelper {
  def Name(value: String): String =
    Token.unapply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid main type: $value")
    }

  def ParamName(name: String): String =
    Token.unapply(name).getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter name: $name")
    }

  def ParamValue(value: String): String =
    Token.unapply(value).orElse(QuotableString.unapply(value)).getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def Params(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParamName(name) -> ParamValue(value) }
}

