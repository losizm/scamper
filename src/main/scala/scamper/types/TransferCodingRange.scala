/*
 * Copyright 2018 Carlos Conyers
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

import CodingHelper._

/**
 * Standardized type for TE header value.
 *
 * @see [[scamper.ImplicitHeaders.TE]]
 */
trait TransferCodingRange {
  /** Gets coding name. */
  def name: String

  /** Gets coding rank. */
  def rank: Float

  /** Gets coding parameters. */
  def params: Map[String, String]

  /** Tests whether name is chunked. */
  def isChunked: Boolean = name == "chunked"

  /** Tests whether name is compress. */
  def isCompress: Boolean = name == "compress"

  /** Tests whether name is deflate. */
  def isDeflate: Boolean = name == "deflate"

  /** Tests whether name is gzip. */
  def isGzip: Boolean = name == "gzip"

  /** Tests whether name is {@code trailers}. */
  def isTrailers: Boolean = name == "trailers"

  /** Tests whether supplied transfer coding matches range. */
  def matches(coding: TransferCoding): Boolean

  /** Returns formatted transfer coding range. */
  override lazy val toString: String = {
    val range = new StringBuilder
    range.append(name)
    if (!isTrailers && rank != 1.0f) range.append("; q=").append(rank)
    if (params.nonEmpty) range.append(FormatParams(params))
    range.toString
  }
}

/** TransferCodingRange factory */
object TransferCodingRange {
  /** Parses formatted transfer coding range. */
  def parse(range: String): TransferCodingRange =
    ParseTransferCoding(range) match {
      case (name, params) =>
        params.collectFirst {
          case (QValue.key(key), QValue.value(value)) => (value.toFloat, (params - key))
        } map {
          case (rank, params) => TransferCodingRangeImpl(Name(name), QValue(rank), Params(params))
        } getOrElse {
          TransferCodingRangeImpl(Name(name), 1.0f, Params(params))
        }
    }

  /** Creates TransferCodingRange with supplied values. */
  def apply(name: String, rank: Float = 1.0f, params: Map[String, String] = Map.empty): TransferCodingRange =
    TransferCodingRangeImpl(Name(name), QValue(rank), Params(params))

  /** Destructures TransferCodingRange. */
  def unapply(range: TransferCodingRange): Option[(String, Float, Map[String, String])] =
    Some((range.name, range.rank, range.params))
}

private case class TransferCodingRangeImpl(name: String, rank: Float, params: Map[String, String]) extends TransferCodingRange {
  def matches(coding: TransferCoding): Boolean =
    name.equalsIgnoreCase(coding.name) && params.forall { case (name, value) => exists(name, value, coding.params) }

  private def exists(name: String, value: String, ps: Map[String, String]): Boolean =
    ps.exists { case (n, v) => name.equalsIgnoreCase(n) && value.equalsIgnoreCase(v) }
}
