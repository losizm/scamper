package scamper.types

import scala.annotation.tailrec
import scamper.Grammar._
import scamper.ListParser

private object LinkParams {
  private val NoValue     = """\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val TokenValue  = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val QuotedValue = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r
  private val BadValue    = """\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*(.+)\s*""".r

  def parse(params: String): Map[String, Option[String]] =
    ListParser(params, true).map {
      case NoValue(name)            => name -> None
      case TokenValue(name, value)  => name -> Some(value)
      case QuotedValue(name, value) => name -> Some(value)
      case BadValue(name, value)    => name -> Some(value)
      case param => throw new IllegalArgumentException(s"Malformed link parameters: $param")
    }.toMap

  def format(params: Map[String, Option[String]]): String =
    if (params.isEmpty) ""
    else
      params.map {
        case (name, Some(value)) => s"; $name=${formatParamValue(value)}"
        case (name, None)        => s"; $name"
      }.mkString

  private def formatParamValue(value: String): String = Token(value).getOrElse('"' + value + '"')
}

