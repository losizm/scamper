package scamper

import scala.annotation.tailrec
import scala.util.matching.Regex.Match

import Grammar._

private object MediaTypeHelper {
  private val MediaTypeRegex     = """\s*([^\s/=;"]+)/([^\s/=;"]+)((?:\s*;\s*.+=.+)*)""".r
  private val UnquotedParamRegex = """\s*;\s*([^\s/=;"]+)\s*=\s*([^\s/=;"]+)\s*""".r
  private val QuotedParamRegex   = """\s*;\s*([^\s/=;"]+)\s*=\s*"([^"]*)"\s*""".r

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

  def ParseMediaType(mediaType: String): (String, String, Map[String, String]) =
    mediaType match {
      case MediaTypeRegex(mainType, subtype, params) => (mainType, subtype, ParseParams(params))
      case _ => throw new IllegalArgumentException(s"Malformed media type: $mediaType")
    }

  @tailrec
  def ParseParams(params: String, include: Map[String, String] = Map.empty): Map[String, String] =
    findPrefixParam(params) match {
      case None =>
        if (params.matches("(\\s*;)?\\s*")) include
        else throw new IllegalArgumentException(s"Malformed media type parameters: $params")
      case Some(m) =>
        ParseParams(m.after.toString, include + (m.group(1) -> m.group(2)))
    }

  def FormatParams(params: Map[String, String]): String =
    params.map { case (name, value) => s"; $name=${quoteParamValue(value)}" }.mkString

  private def findPrefixParam(s: String): Option[Match] =
    UnquotedParamRegex.findPrefixMatchOf(s).orElse(QuotedParamRegex.findPrefixMatchOf(s))

  private def quoteParamValue(value: String): String = Token(value).getOrElse('"' + value + '"')
}

