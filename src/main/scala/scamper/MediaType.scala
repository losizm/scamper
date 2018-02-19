package scamper

import scala.annotation.tailrec
import scala.util.matching.Regex.Match

import MediaTypeHelper._
import Grammar._

/** Internet media type */
case class MediaType private (primaryType: String, subtype: String, params: Map[String, String]) {
  /** Returns formatted media type. */
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
    params.map(param => s"; ${param._1}=${quote(param._2)}").mkString

  private def quote(value: String): String =
    Token.unapply(value).getOrElse('"' + value + '"')
}

/** MediaType factory */
object MediaType {
  private val withoutParams     = """\s*([^\s/=;"]+)/([^\s/=;"]+)\s*""".r
  private val withParams        = """\s*([^\s/=;"]+)/([^\s/=;"]+)\s*(;.*)\s*""".r
  private val withUnquotedValue = """\s*;\s*([^\s/=;"]+)\s*=\s*([^\s/=;"]+)\s*""".r
  private val withQuotedValue   = """\s*;\s*([^\s/=;"]+)\s*=\s*"([^"]*)"\s*""".r

  /** Creates MediaType using supplied attributes. */
  def apply(primaryType: String, subtype: String, params: Map[String, String]): MediaType =
    new MediaType(PrimaryType(primaryType), Subtype(subtype), Params(params))

  /** Creates MediaType using supplied attributes. */
  def apply(primaryType: String, subtype: String, params: (String, String)*): MediaType =
    apply(primaryType, subtype, params.toMap)

  /** Parses formatted media type. */
  def apply(mediaType: String): MediaType =
    mediaType match {
      case withoutParams(primary, sub) => MediaType(primary, sub)
      case withParams(primary, sub, params) => MediaType(primary, sub, parseParams(params))
      case _ => throw new IllegalArgumentException(s"Malformed media type: $mediaType")
    }

  @tailrec
  private def parseParams(s: String, params: Map[String, String] = Map.empty): Map[String, String] =
    findPrefixParam(s) match {
      case None =>
        if (s.matches("(\\s*;)?\\s*")) params
        else throw new IllegalArgumentException(s"Malformed media type parameters: $params")

      case Some(m) =>
        parseParams(m.after.toString, params + (m.group(1) -> m.group(2)))
    }

  private def findPrefixParam(s: String): Option[Match] =
    withUnquotedValue.findPrefixMatchOf(s).orElse(withQuotedValue.findPrefixMatchOf(s))
}

