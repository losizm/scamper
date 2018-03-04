package scamper

import MediaTypeHelper._

/**
 * Internet media type
 *
 * @see [[ImplicitHeaders.ContentType]]
 */
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

  /** Converts to MediaRange with supplied weight. */
  def toRange(weight: Float = 1.0f): MediaRange =
    MediaRange(mainType, subtype, weight, params)

  /** Returns formatted media type. */
  override lazy val toString: String = mainType + '/' + subtype + FormatParams(params)
}

/** MediaType factory */
object MediaType {
  /** Parses formatted media type. */
  def apply(mediaType: String): MediaType =
    ParseMediaType(mediaType) match {
      case (mainType, subtype, params) => apply(mainType, subtype, params)
    }

  /** Creates MediaType with supplied values. */
  def apply(mainType: String, subtype: String, params: Map[String, String]): MediaType =
    new MediaTypeImpl(MainType(mainType), Subtype(subtype), Params(params))

  /** Creates MediaType with supplied values. */
  def apply(mainType: String, subtype: String, params: (String, String)*): MediaType =
    apply(mainType, subtype, params.toMap)

  /** Destructures MediaType. */
  def unapply(mediaType: MediaType): Option[(String, String, Map[String, String])] =
    Some((mediaType.mainType, mediaType.subtype, mediaType.params))
}

private class MediaTypeImpl(val mainType: String, val subtype: String, val params: Map[String, String]) extends MediaType

