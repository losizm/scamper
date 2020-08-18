/*
 * Copyright 2017-2020 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper.types

import java.io.File
import java.util.Properties

import scala.util.{ Success, Try }

import scamper.CollectionConverters._

import MediaTypeHelper._

/**
 * Standardized type for Content-Type header value.
 *
 * @see [[scamper.headers.ContentType]]
 */
trait MediaType {
  /** Gets main type of media type. */
  def mainType: String

  /** Gets subtype of media type. */
  def subtype: String

  /** Gets media type parameters. */
  def params: Map[String, String]

  /** Tests main type for text. */
  def isText: Boolean = mainType == "text"

  /** Tests main type for audio. */
  def isAudio: Boolean = mainType == "audio"

  /** Tests main type for video. */
  def isVideo: Boolean = mainType == "video"

  /** Tests main type for image. */
  def isImage: Boolean = mainType == "image"

  /** Tests main type for font. */
  def isFont: Boolean = mainType == "font"

  /** Tests main type for application. */
  def isApplication: Boolean = mainType == "application"

  /** Tests main type for multipart. */
  def isMultipart: Boolean = mainType == "multipart"

  /** Tests main type for message. */
  def isMessage: Boolean = mainType == "message"

  /** Converts to range with supplied weight. */
  def toRange(weight: Float = 1.0f): MediaRange =
    MediaRange(mainType, subtype, weight, params)

  /** Returns formatted media type. */
  override lazy val toString: String = mainType + '/' + subtype + FormatParams(params)
}

/** Provides factory for `MediaType`. */
object MediaType {
  private val mappings: Map[String, MediaType] = Try {
    val props = new Properties()
    val in = getClass.getResourceAsStream("media-types.properties")

    try props.load(in)
    finally Try(in.close())

    asScala(props).map {
      case (key, value) => key.toLowerCase -> Try(apply(value))
    }.collect {
      case (key, Success(value)) => key -> value
    }.toMap
  }.getOrElse(Map.empty)

  private val fileNamePattern = ".+\\.(\\w+)".r

  /** Gets media type for given file. */
  def fromFile(file: File): Option[MediaType] =
    fromFileName(file.getName)

  /** Gets media type for given file name. */
  def fromFileName(fileName: String): Option[MediaType] =
    fileName match {
      case fileNamePattern(suffix) => fromSuffix(suffix)
      case _ => None
    }

  /** Gets media type for given file name suffix. */
  def fromSuffix(suffix: String): Option[MediaType] =
    mappings.get(suffix.toLowerCase)

  /** Parses formatted media type. */
  def apply(mediaType: String): MediaType =
    ParseMediaType(mediaType) match {
      case (mainType, subtype, params) => apply(mainType, subtype, params)
    }

  /** Creates media type with supplied values. */
  def apply(mainType: String, subtype: String, params: Map[String, String]): MediaType =
    MediaTypeImpl(MainType(mainType), Subtype(subtype), Params(params))

  /** Creates media type with supplied values. */
  def apply(mainType: String, subtype: String, params: (String, String)*): MediaType =
    apply(mainType, subtype, params.toMap)

  /** Destructures media type. */
  def unapply(mediaType: MediaType): Option[(String, String, Map[String, String])] =
    Some((mediaType.mainType, mediaType.subtype, mediaType.params))
}

private case class MediaTypeImpl(mainType: String, subtype: String, params: Map[String, String]) extends MediaType
