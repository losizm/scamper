package scamper

import scala.annotation.tailrec
import scala.util.matching.Regex.Match

import Grammar._

private object TransferCodingHelper {
  private val CodingRegex        = """([^\s/=;"]+)((?:\s*;\s*.+=.+)*)""".r
  private val UnquotedParamRegex = """\s*;\s*([^\s/=;"]+)\s*=\s*([^\s/=;"]+)\s*""".r
  private val QuotedParamRegex   = """\s*;\s*([^\s/=;"]+)\s*=\s*"([^"]*)"\s*""".r

  def Name(name: String): String =
    Token(name).map(_.toLowerCase) getOrElse {
      throw new IllegalArgumentException(s"Invalid name: $name")
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

  def ParseTransferCoding(coding: String): (String, Map[String, String]) =
    coding match {
      case CodingRegex(name, params) => (name.trim, ParseParams(params))
      case _ => throw new IllegalArgumentException(s"Malformed transfer coding: $coding")
    }

  @tailrec
  def ParseParams(params: String, include: Map[String, String] = Map.empty): Map[String, String] =
    findPrefixParam(params) match {
      case None =>
        if (params.matches("(\\s*;)?\\s*")) include
        else throw new IllegalArgumentException(s"Malformed transfer coding parameters: $params")
      case Some(m) =>
        ParseParams(m.after.toString, include + (m.group(1) -> m.group(2)))
    }

  def FormatParams(params: Map[String, String]): String =
    params.map { case (name, value) => s"; $name=${quoteParamValue(value)}" }.mkString

  private def findPrefixParam(s: String): Option[Match] =
    UnquotedParamRegex.findPrefixMatchOf(s).orElse(QuotedParamRegex.findPrefixMatchOf(s))

  private def quoteParamValue(value: String) = Token(value).getOrElse('"' + value + '"')
}

