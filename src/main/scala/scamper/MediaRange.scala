package scamper

import scala.annotation.tailrec
import scala.util.matching.Regex

import MediaTypeHelper._

/** Internet media range */
trait MediaRange extends MediaType {
  /** Gets quality value of media range */
  def qvalue: Float

  /** Tests whether main type is wildcard. */
  def isWildcard: Boolean = mainType == "*"

  /** Tests whether supplied media type matches range. */
  def matches(mediaType: MediaType): Boolean

  /** Returns formatted media range. */
  override lazy val toString: String = mainType + '/' + subtype + "; q=" + qvalue + FormatParams(params)
}

/** MediaRange factory */
object MediaRange {
  private val qkeyRegex = "([Qq])".r
  private val qvalueRegex = """(\d+(?:\.\d*))""".r

  /** Parse formatted media range. */
  def apply(mediaRange: String): MediaRange = {
    ParseMediaType(mediaRange) match {
      case (mainType, subtype, params) =>
        params.collectFirst {
          case (qkeyRegex(key), qvalueRegex(value)) => (value.toFloat, (params - key))
        } map {
          case (qvalue, params) => new MediaRangeImpl(MainType(mainType), Subtype(subtype), Qvalue(qvalue), Params(params))
        } getOrElse {
          new MediaRangeImpl(MainType(mainType), Subtype(subtype), 1.0f, Params(params))
        }
    }
  }

  /** Creates MediaRange from supplied attributes. */
  def apply(mainType: String, subtype: String, qvalue: Float = 1.0f, params: Map[String, String] = Map.empty): MediaRange =
    new MediaRangeImpl(MainType(mainType), Subtype(subtype), Qvalue(qvalue), Params(params))

  /** Destructures MediaRange. */
  def unapply(mediaRange: MediaRange): Option[(String, String, Float, Map[String, String])] =
    Some((mediaRange.mainType, mediaRange.subtype, mediaRange.qvalue, mediaRange.params))

  private def Qvalue(qvalue: Float): Float =
    (qvalue.max(0f).min(1f) * 1000).floor / 1000
}

private class MediaRangeImpl(val mainType: String, val subtype: String, val qvalue: Float, val params: Map[String, String]) extends MediaRange {
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

