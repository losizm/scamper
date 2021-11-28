/*
 * Copyright 2021 Carlos Conyers
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
package scamper
package http
package types

import java.io.File
import java.util.Properties

import scala.util.{ Success, Try }

import CollectionConverters.*
import MediaTypeHelper.*

/**
 * Standardized type for Content-Type header value.
 *
 * @see [[scamper.http.headers.ContentType]]
 */
trait MediaType:
  /** Gets type name of media type. */
  def typeName: String

  /** Gets subtype name of media type. */
  def subtypeName: String

  /** Gets media type parameters. */
  def params: Map[String, String]

  /** Tests type name for text. */
  def isText: Boolean = typeName == "text"

  /** Tests type name for audio. */
  def isAudio: Boolean = typeName == "audio"

  /** Tests type name for video. */
  def isVideo: Boolean = typeName == "video"

  /** Tests type name for image. */
  def isImage: Boolean = typeName == "image"

  /** Tests type name for font. */
  def isFont: Boolean = typeName == "font"

  /** Tests type name for application. */
  def isApplication: Boolean = typeName == "application"

  /** Tests type name for multipart. */
  def isMultipart: Boolean = typeName == "multipart"

  /** Tests type name for message. */
  def isMessage: Boolean = typeName == "message"

  /** Creates media types with new parameters. */
  def setParams(params: Map[String, String]): MediaType =
    MediaType(typeName, subtypeName, params)

  /** Creates media types with new parameters. */
  def setParams(params: (String, String)*): MediaType =
    MediaType(typeName, subtypeName, params.toMap)

  /** Converts to range with supplied weight. */
  def toRange(weight: Float = 1.0f): MediaRange =
    MediaRange(typeName, subtypeName, weight, params)

  /** Returns formatted media type. */
  override lazy val toString: String = typeName + '/' + subtypeName + FormatParams(params)

/** Provides factory for `MediaType`. */
object MediaType:
  private val mappings: Map[String, MediaType] = Try {
    val props = Properties()
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

  /** `text/plain` */
  val plain = MediaType("text/plain")

  /** `application/octet-stream` */
  val octetStream = MediaType("application/octet-stream")

  /** Gets media type for given file. */
  def forFile(file: File): Option[MediaType] =
    forFileName(file.getName)

  /** Gets media type for given file name. */
  def forFileName(fileName: String): Option[MediaType] =
    fileName match
      case fileNamePattern(suffix) => forSuffix(suffix)
      case _ => None

  /**
   * Gets media type for given file name suffix.
   *
   * @note Leading `"."` (if present) is stripped before locating media type.
   */
  def forSuffix(suffix: String): Option[MediaType] =
    mappings.get(suffix.stripPrefix(".").toLowerCase)

  /** Parses formatted media type. */
  def apply(mediaType: String): MediaType =
    ParseMediaType(mediaType) match
      case (typeName, subtypeName, params) => apply(typeName, subtypeName, params)

  /** Creates media type with supplied values. */
  def apply(typeName: String, subtypeName: String, params: Map[String, String]): MediaType =
    MediaTypeImpl(TypeName(typeName), SubtypeName(subtypeName), Params(params))

  /** Creates media type with supplied values. */
  def apply(typeName: String, subtypeName: String, params: (String, String)*): MediaType =
    apply(typeName, subtypeName, params.toMap)

private case class MediaTypeImpl(typeName: String, subtypeName: String, params: Map[String, String]) extends MediaType
