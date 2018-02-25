package scamper

import Grammar._

private object MediaTypeHelper {
  def MainType(mainType: String): String =
    Token(mainType) getOrElse {
      throw new IllegalArgumentException(s"Invalid main type: $mainType")
    }

  def Subtype(subtype: String): String =
    Token(subtype) getOrElse {
      throw new IllegalArgumentException(s"Invalid subtype: $subtype")
    }

  def ParamName(name: String): String =
    Token(name) getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter name: $name")
    }

  def ParamValue(value: String): String =
    Token(value) orElse QuotableString(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid parameter value: $value")
    }

  def Params(params: Map[String, String]): Map[String, String] =
    params.map { case (name, value) => ParamName(name) -> ParamValue(value) }
}

