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

import CodingHelper.*

/**
 * Standardized type for Transfer-Encoding header value.
 *
 * @see [[scamper.http.headers.TransferEncoding]]
 */
trait TransferCoding:
  /** Gets coding name. */
  def name: String

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

  /** Returns formatted transfer coding. */
  override lazy val toString: String = name + FormatParams(params)

/** Provides factory for `TransferCoding`. */
object TransferCoding:
  /** Parses formatted transfer coding. */
  def parse(coding: String): TransferCoding =
    ParseTransferCoding(coding) match
      case (name, params) => apply(name, params)

  /** Creates transfer coding with supplied values. */
  def apply(name: String, params: Map[String, String]): TransferCoding =
    TransferCodingImpl(Name(name), Params(params))

  /** Creates transfer coding with supplied values. */
  def apply(name: String, params: (String, String)*): TransferCoding =
    apply(name, params.toMap)

private case class TransferCodingImpl(name: String, params: Map[String, String]) extends TransferCoding
