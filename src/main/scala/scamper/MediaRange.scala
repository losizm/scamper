package scamper

import scala.annotation.tailrec
import scala.util.matching.Regex

import MediaTypeHelper._

/** Media Range */
trait MediaRange {
  /** Gets weight of media range */
  def weight: Float

  /** Main type of media range */
  def mainType: String

  /** Subtype of media range */
  def subtype: String

  /** Media range parameters */
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

  /** Tests whether main type is wildcard (*). */
  def isWildcard: Boolean = mainType == "*"

  /** Tests whether supplied media type matches range. */
  def matches(mediaType: MediaType): Boolean

  /** Returns formatted media range. */
  override lazy val toString: String = mainType + '/' + subtype + "; q=" + weight + FormatParams(params)
}

/** MediaRange factory */
object MediaRange {
  /** Parse formatted media range. */
  def apply(mediaRange: String): MediaRange =
    ParseMediaType(mediaRange) match {
      case (mainType, subtype, params) =>
        params.collectFirst {
          case (QValue.key(key), QValue.value(value)) => (value.toFloat, (params - key))
        } map {
          case (weight, params) => new MediaRangeImpl(MainType(mainType), Subtype(subtype), QValue(weight), Params(params))
        } getOrElse {
          new MediaRangeImpl(MainType(mainType), Subtype(subtype), 1.0f, Params(params))
        }
    }

  /** Creates MediaRange with supplied values. */
  def apply(mainType: String, subtype: String, weight: Float = 1.0f, params: Map[String, String] = Map.empty): MediaRange =
    new MediaRangeImpl(MainType(mainType), Subtype(subtype), QValue(weight), Params(params))

  /** Destructures MediaRange. */
  def unapply(mediaRange: MediaRange): Option[(String, String, Float, Map[String, String])] =
    Some((mediaRange.mainType, mediaRange.subtype, mediaRange.weight, mediaRange.params))
}

private class MediaRangeImpl(val mainType: String, val subtype: String, val weight: Float, val params: Map[String, String]) extends MediaRange {
  private val range = (regex(mainType) + "/" + regex(subtype)).r

  def matches(mediaType: MediaType): Boolean =
    (mediaType.mainType + "/" + mediaType.subtype) match {
      case range(_*) => params.forall { case (name, value) => exists(name, value, mediaType.params) }
      case _ => false
    }

  private def exists(name: String, value: String, ps: Map[String, String]): Boolean =
    ps.exists {
      case (n, v) => name.equalsIgnoreCase(n) && value.equalsIgnoreCase(v)
    }

  private def regex(value: String): String =
    if (value.equals("*")) ".+"
    else Regex.quote(value)
}

