package scamper.types

import scala.annotation.tailrec
import scamper.Grammar._

private object StandardParams {
  private val TokenParam = """\s*;\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*([\w!#$%&'*+.^`|~-]+)\s*""".r
  private val QuotedParam = """\s*;\s*([\w!#$%&'*+.^`|~-]+)\s*=\s*"([^"]*)"\s*""".r

  def parse(params: String): Map[String, String] =
    parseParams(params, Map.empty)

  def format(params: Map[String, String]): String =
    params.map { case (name, value) => s"; $name=${formatParamValue(value)}" }.mkString

  @tailrec
  private def parseParams(params: String, collected: Map[String, String]): Map[String, String] =
    findPrefixParam(params) match {
      case None =>
        if (params.matches("\\s*")) collected
        else throw new IllegalArgumentException(s"Malformed parameters: $params")
      case Some((name, value, suffix)) => parseParams(suffix, collected + (name -> value))
    }

  private def findPrefixParam(text: String): Option[(String, String, String)] =
    TokenParam.findPrefixMatchOf(text).orElse(QuotedParam.findPrefixMatchOf(text)).map { m =>
      (m.group(1), m.group(2), m.after.toString)
    }

  private def formatParamValue(value: String): String = Token(value).getOrElse('"' + value + '"')
}
