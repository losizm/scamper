package scamper

import scala.annotation.tailrec
import scala.util.matching.Regex.Match

import ContentTypeHelper._
import Grammar._

/** HTTP Content-Type */
case class ContentType private (primaryType: String, subtype: String, parameters: Map[String, String]) {
  /** Returns formatted content type. */
  override val toString: String = s"$primaryType/$subtype$paramsToString"

  /** Tests whether primary type is text. */
  def isText: Boolean = primaryType == "text"

  /** Tests whether primary type is audio. */
  def isAudio: Boolean = primaryType == "audio"

  /** Tests whether primary type is video. */
  def isVideo: Boolean = primaryType == "video"

  /** Tests whether primary type is application. */
  def isApplication: Boolean = primaryType == "application"

  /** Tests whether primary type is multipart. */
  def isMultipart: Boolean = primaryType == "multipart"

  /** Tests whether primary type is message. */
  def isMessage: Boolean = primaryType == "message"

  private def paramsToString: String =
    parameters.map(param => s"; ${param._1}=${quote(param._2)}").mkString

  private def quote(value: String): String =
    Token.unapply(value).getOrElse('"' + value + '"')
}

/** ContentType factory */
object ContentType {
  private val withoutParams     = """\s*([^\s/=;"]+)/([^\s/=;"]+)\s*""".r
  private val withParams        = """\s*([^\s/=;"]+)/([^\s/=;"]+)\s*(;.*)\s*""".r
  private val withUnquotedValue = """\s*;\s*([^\s/=;"]+)\s*=\s*([^\s/=;"]+)\s*""".r
  private val withQuotedValue   = """\s*;\s*([^\s/=;"]+)\s*=\s*"([^"]*)"\s*""".r

  /** Creates ContentType using supplied attributes. */
  def apply(primaryType: String, subtype: String, parameters: Map[String, String]): ContentType =
    new ContentType(PrimaryType(primaryType), Subtype(subtype), Parameters(parameters))

  /** Creates ContentType using supplied attributes. */
  def apply(primaryType: String, subtype: String, parameters: (String, String)*): ContentType =
    apply(primaryType, subtype, parameters.toMap)

  /** Parses formatted content type. */
  def apply(contentType: String): ContentType =
    contentType match {
      case withoutParams(primary, sub) => ContentType(primary, sub)
      case withParams(primary, sub, params) => ContentType(primary, sub, parseParameters(params))
      case _ => throw new IllegalArgumentException(s"Malformed content type: $contentType")
    }

  @tailrec
  private def parseParameters(s: String, params: Map[String, String] = Map.empty): Map[String, String] =
    findPrefixParameter(s) match {
      case None =>
        if (s.matches("(\\s*;)?\\s*")) params
        else throw new IllegalArgumentException(s"Malformed content type parameters: $params")

      case Some(m) =>
        parseParameters(m.after.toString, params ++ Map(m.group(1) -> m.group(2)))
    }

  private def findPrefixParameter(s: String): Option[Match] =
    withUnquotedValue.findPrefixMatchOf(s).orElse(withQuotedValue.findPrefixMatchOf(s))
}

