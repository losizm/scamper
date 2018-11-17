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
 * Standardized type for Transfer-Encoding header value.
 *
 * @see [[scamper.headers.TransferEncoding]]
 */
trait TransferCoding {
  /** Gets coding name. */
  def name: String

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

  /** Returns formatted transfer coding. */
  override lazy val toString: String = name + FormatParams(params)
}

/** TransferCoding factory */
object TransferCoding {
  /** Parses formatted transfer coding. */
  def parse(coding: String): TransferCoding =
    ParseTransferCoding(coding) match {
      case (name, params) => apply(name, params)
    }

  /** Creates TransferCoding with supplied values. */
  def apply(name: String, params: Map[String, String]): TransferCoding =
    TransferCodingImpl(Name(name), Params(params))

  /** Creates TransferCoding with supplied values. */
  def apply(name: String, params: (String, String)*): TransferCoding =
    apply(name, params.toMap)

  /** Destructures TransferCoding. */
  def unapply(coding: TransferCoding): Option[(String, Map[String, String])] =
    Some((coding.name, coding.params))
}

private case class TransferCodingImpl(name: String, params: Map[String, String]) extends TransferCoding
