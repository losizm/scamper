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
  /** Gets weight of media range. */
  def weight: Float

  /** Gets main type of media range. */
  def mainType: String

  /** Gets subtype of media range. */
  def subtype: String

  /** Gets media range parameters. */
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

  /** Tests main type for wildcard (*). */
  def isWildcard: Boolean = mainType == "*"

  /** Tests whether range matches supplied media type. */
  def matches(mediaType: MediaType): Boolean

  /** Returns formatted range. */
  override lazy val toString: String =
    if weight == 1.0f then mainType + '/' + subtype + FormatParams(params)
    else mainType + '/' + subtype + "; q=" + weight + FormatParams(params)

/** Provides factory for `MediaRange`. */
object MediaRange:
  /** Parses formatted range. */
  def apply(mediaRange: String): MediaRange =
    ParseMediaType(mediaRange) match
      case (mainType, subtype, params) =>
        params.collectFirst {
          case (QValue.key(key), QValue.value(value)) => (value.toFloat, params - key)
        } map {
          case (weight, params) => MediaRangeImpl(MainType(mainType), Subtype(subtype), QValue(weight), Params(params))
        } getOrElse {
          MediaRangeImpl(MainType(mainType), Subtype(subtype), 1.0f, Params(params))
        }

  /** Creates range with supplied values. */
  def apply(mainType: String, subtype: String, weight: Float = 1.0f, params: Map[String, String] = Map.empty): MediaRange =
    MediaRangeImpl(MainType(mainType), Subtype(subtype), QValue(weight), Params(params))

private case class MediaRangeImpl(mainType: String, subtype: String, weight: Float, params: Map[String, String]) extends MediaRange:
  private val range = (regex(mainType) + "/" + regex(subtype)).r

  def matches(mediaType: MediaType): Boolean =
    ((mediaType.mainType + "/" + mediaType.subtype) match
      case range(_*) => params.forall { case (name, value) => exists(name, value, mediaType.params) }
      case _ => false
    ) && weight > 0

  private def exists(name: String, value: String, ps: Map[String, String]): Boolean =
    ps.exists {
      case (n, v) => name.equalsIgnoreCase(n) && value.equalsIgnoreCase(v)
    }

  private def regex(value: String): String =
    if value.equals("*") then ".+"
    else Regex.quote(value)
