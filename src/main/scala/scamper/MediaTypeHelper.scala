package scamper

import Grammar._

private object MediaTypeHelper {
  def PrimaryType(value: String): String =
    Token.unapply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid primary type: $value")
    }

  def Subtype(value: String): String =
    Token.unapply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid primary type: $value")
    }

  def ParamAttribute(value: String): String =
    Token.unapply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter attribute: $value")
    }

  def ParamValue(value: String): String =
    Token.unapply(value).orElse(QuotableString.unapply(value)).getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def Params(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParamAttribute(name) -> ParamValue(value) }
}

