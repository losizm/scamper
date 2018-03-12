package scamper

import scala.annotation.tailrec
import scamper.Grammar._

private object AuthParams {
  private val TokenParam = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val QuotedParam = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r

  def parse(params: String): Map[String, String] =
    ListParser(params).map {
      case TokenParam(name, value)  => name -> value
      case QuotedParam(name, value) => name -> value
      case param => throw new IllegalArgumentException(s"Malformed auth parameters: $param")
    }.toMap

  def format(params: Map[String, String]): String =
    if (params.isEmpty) ""
    else params.map { case (name, value) => s"$name=${formatParamValue(value)}" }.mkString(" ", ", ", "")

  private def formatParamValue(value: String): String = Token(value).getOrElse('"' + value + '"')
}

