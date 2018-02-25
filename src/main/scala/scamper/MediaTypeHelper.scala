package scamper

import Grammar._

private object MediaTypeHelper {
  def MainType(value: String): String =
    Token(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid main type: $value")
    }

  def Subtype(value: String): String =
    Token(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid subtype: $value")
    }

  def ParamName(value: String): String =
    Token(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter name: $value")
    }

  def ParamValue(value: String): String =
    Token(value) orElse QuotableString(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def Params(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParamName(name) -> ParamValue(value) }
}

