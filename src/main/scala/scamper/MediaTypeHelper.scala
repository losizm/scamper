package scamper

import Grammar._

private object MediaTypeHelper {
  private val syntax = """\s*([\w!#$%&'*+.^`|~-]+)/([\w!#$%&'*+.^`|~-]+)(\s*(?:;.*)?)""".r

  def MainType(mainType: String): String =
    Token(mainType) getOrElse {
      throw new IllegalArgumentException(s"Invalid main type: $mainType")
    }

  def Subtype(subtype: String): String =
    Token(subtype) getOrElse {
      throw new IllegalArgumentException(s"Invalid subtype: $subtype")
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

  def ParseMediaType(mediaType: String): (String, String, Map[String, String]) =
    mediaType match {
      case syntax(mainType, subtype, params) => (mainType, subtype, ParseParams(params))
      case _ => throw new IllegalArgumentException(s"Malformed media type: $mediaType")
    }

  def ParseParams(params: String): Map[String, String] =
    StandardParams.parse(params)

  def FormatParams(params: Map[String, String]): String =
    StandardParams.format(params)
}

