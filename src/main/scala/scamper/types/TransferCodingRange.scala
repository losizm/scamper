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
package scamper.types

import CodingHelper.*

/**
 * Standardized type for TE header value.
 *
 * @see [[scamper.headers.TE]]
 */
trait TransferCodingRange:
  /** Gets coding name. */
  def name: String

  /** Gets coding weight. */
  def weight: Float

  /** Gets coding parameters. */
  def params: Map[String, String]

  /** Tests for chunked. */
  def isChunked: Boolean = name == "chunked"

  /** Tests for compress. */
  def isCompress: Boolean = name == "compress"

  /** Tests for deflate. */
  def isDeflate: Boolean = name == "deflate"

  /** Tests for gzip. */
  def isGzip: Boolean = name == "gzip"

  /** Tests for trailers. */
  def isTrailers: Boolean = name == "trailers"

  /** Tests whether range matches supplied transfer coding. */
  def matches(coding: TransferCoding): Boolean

  /** Returns formatted range. */
  override lazy val toString: String =
    val range = StringBuilder()
    range.append(name)
    if !isTrailers && weight != 1.0f then range.append("; q=").append(weight)
    if params.nonEmpty then range.append(FormatParams(params))
    range.toString

/** Provides factory for `TransferCodingRange`. */
object TransferCodingRange:
  /** Parses formatted range. */
  def parse(range: String): TransferCodingRange =
    ParseTransferCoding(range) match
      case (name, params) =>
        params.collectFirst {
          case (QValue.key(key), QValue.value(value)) => (value.toFloat, (params - key))
        } map {
          case (weight, params) => TransferCodingRangeImpl(Name(name), QValue(weight), Params(params))
        } getOrElse {
          TransferCodingRangeImpl(Name(name), 1.0f, Params(params))
        }

  /** Creates range with supplied values. */
  def apply(name: String, weight: Float = 1.0f, params: Map[String, String] = Map.empty): TransferCodingRange =
    TransferCodingRangeImpl(Name(name), QValue(weight), Params(params))

private case class TransferCodingRangeImpl(name: String, weight: Float, params: Map[String, String]) extends TransferCodingRange:
  def matches(coding: TransferCoding): Boolean =
    name.equalsIgnoreCase(coding.name) && params.forall { case (name, value) => exists(name, value, coding.params) } && weight > 0

  private def exists(name: String, value: String, ps: Map[String, String]): Boolean =
    ps.exists { case (n, v) => name.equalsIgnoreCase(n) && value.equalsIgnoreCase(v) }
