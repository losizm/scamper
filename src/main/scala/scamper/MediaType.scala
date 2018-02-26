package scamper

import scala.annotation.tailrec
import scala.util.matching.Regex.Match

import MediaTypeHelper._
import Grammar._

/** Internet media type */
trait MediaType {
  /** Main type of media type */
  def mainType: String

  /** Subtype of media type */
  def subtype: String

  /** Media type parameters */
  def params: Map[String, String]

  /** Tests whether main type is text. */
  def isText: Boolean = mainType == "text"

  /** Tests whether main type is audio. */
  def isAudio: Boolean = mainType == "audio"

  /** Tests whether main type is video. */
  def isVideo: Boolean = mainType == "video"

  /** Tests whether main type is image. */
  def isImage: Boolean = mainType == "image"

  /** Tests whether main type is font. */
  def isFont: Boolean = mainType == "font"

  /** Tests whether main type is application. */
  def isApplication: Boolean = mainType == "application"

  /** Tests whether main type is multipart. */
  def isMultipart: Boolean = mainType == "multipart"

  /** Tests whether main type is message. */
  def isMessage: Boolean = mainType == "message"

  /** Returns formatted media type. */
  override lazy val toString: String = mainType + '/' + subtype + formatParams

  private def formatParams: String = {
    def quote(value: String) = Token(value).getOrElse('"' + value + '"')
    params.map { case (name, value) => s"; $name=${quote(value)}" }.mkString
  }
}

/** MediaType factory */
object MediaType {
  private val withoutParams     = """\s*([^\s/=;"]+)/([^\s/=;"]+)\s*""".r
  private val withParams        = """\s*([^\s/=;"]+)/([^\s/=;"]+)\s*(;.*)\s*""".r
  private val withUnquotedValue = """\s*;\s*([^\s/=;"]+)\s*=\s*([^\s/=;"]+)\s*""".r
  private val withQuotedValue   = """\s*;\s*([^\s/=;"]+)\s*=\s*"([^"]*)"\s*""".r

  /** Creates MediaType using supplied attributes. */
  def apply(mainType: String, subtype: String, params: Map[String, String]): MediaType =
    new MediaTypeImpl(MainType(mainType), Subtype(subtype), Params(params))

  /** Creates MediaType using supplied attributes. */
  def apply(mainType: String, subtype: String, params: (String, String)*): MediaType =
    apply(mainType, subtype, params.toMap)

  /** Parses formatted media type. */
  def apply(mediaType: String): MediaType =
    mediaType match {
      case withoutParams(mainType, subtype) => MediaType(mainType, subtype)
      case withParams(mainType, subtype, params) => MediaType(mainType, subtype, parseParams(params))
      case _ => throw new IllegalArgumentException(s"Malformed media type: $mediaType")
    }

  /** Destructures MediaType. */
  def unapply(mediaType: MediaType): Option[(String, String, Map[String, String])] =
    Some((mediaType.mainType, mediaType.subtype, mediaType.params))

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

private class MediaTypeImpl(val mainType: String, val subtype: String, val params: Map[String, String]) extends MediaType

