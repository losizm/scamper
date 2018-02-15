package scamper

import Grammar._

private object ContentTypeHelper {
  def PrimaryType(value: String): String =
    Token.unapply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid primary type: $value")
    }

  def Subtype(value: String): String =
    Token.unapply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid primary type: $value")
    }

  def ParameterAttribute(value: String): String =
    Token.unapply(value).getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter attribute: $value")
    }

  def ParameterValue(value: String): String =
    Token.unapply(value).orElse(QuotableString.unapply(value)).getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def Parameters(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParameterAttribute(name) -> ParameterValue(value) }
}

