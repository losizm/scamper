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

import scala.util.matching.Regex

import MediaTypeHelper.*

/**
 * Standardized type for Accept header value.
 *
 * @see [[scamper.http.headers.Accept]]
 */
trait MediaRange:
  /** Gets weight. */
  def weight: Float

  /** Gets type name. */
  def typeName: String

  /** Gets subtype name. */
  def subtypeName: String

  /** Gets full name. */
  def fullName: String =
    typeName + '/' + subtypeName

  /** Gets parameters. */
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

  /** Tests type name for wildcard (*). */
  def isWildcard: Boolean = typeName == "*"

  /** Tests whether range matches supplied media type. */
  def matches(mediaType: MediaType): Boolean

  /** Returns formatted range. */
  override lazy val toString: String =
    weight == 1.0f match
      case true  => fullName + FormatParams(params)
      case false => fullName + "; q=" + weight + FormatParams(params)

/** Provides factory for `MediaRange`. */
object MediaRange:
  /** Parses formatted range. */
  def apply(mediaRange: String): MediaRange =
    ParseMediaType(mediaRange) match
      case (typeName, subtypeName, params) =>
        params.collectFirst {
          case (QValue.key(key), QValue.value(value)) => (value.toFloat, params - key)
        } map {
          case (weight, params) => MediaRangeImpl(TypeName(typeName), SubtypeName(subtypeName), QValue(weight), Params(params))
        } getOrElse {
          MediaRangeImpl(TypeName(typeName), SubtypeName(subtypeName), 1.0f, Params(params))
        }

  /** Creates range with supplied values. */
  def apply(typeName: String, subtypeName: String, weight: Float = 1.0f, params: Map[String, String] = Map.empty): MediaRange =
    MediaRangeImpl(TypeName(typeName), SubtypeName(subtypeName), QValue(weight), Params(params))

private case class MediaRangeImpl(typeName: String, subtypeName: String, weight: Float, params: Map[String, String]) extends MediaRange:
  private val range = (regex(typeName) + "/" + regex(subtypeName)).r

  def matches(mediaType: MediaType): Boolean =
    (mediaType.fullName match
      case range(_*) => params.forall { case (name, value) => exists(name, value, mediaType.params) }
      case _         => false
    ) && weight > 0

  private def exists(name: String, value: String, ps: Map[String, String]): Boolean =
    ps.exists {
      case (n, v) => name.equalsIgnoreCase(n) && value.equalsIgnoreCase(v)
    }

  private def regex(value: String): String =
    if value.equals("*") then ".+"
    else Regex.quote(value)
