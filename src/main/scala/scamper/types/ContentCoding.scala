/*
 * Copyright 2019 Carlos Conyers
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

import CodingHelper.Name

/**
 * Standardized type for Content-Encoding header value.
 *
 * @see [[scamper.headers.ContentEncoding]]
 */
trait ContentCoding {
  /** Gets coding name. */
  def name: String

  /** Tests whether name is compress. */
  def isCompress: Boolean = name == "compress"

  /** Tests whether name is deflate. */
  def isDeflate: Boolean = name == "deflate"

  /** Tests whether name is gzip. */
  def isGzip: Boolean = name == "gzip"

  /** Tests whether name is identity. */
  def isIdentity: Boolean = name == "identity"

  /** Converts to ContentCodingRange with supplied weight. */
  def toRange(weight: Float = 1.0f): ContentCodingRange =
    ContentCodingRange(name, weight)

  /** Returns content coding name. */
  override val toString: String = name
}

/** ContentCoding factory */
object ContentCoding {
  /** Creates ContentCoding with supplied name. */
  def apply(name: String): ContentCoding =
    ContentCodingImpl(Name(name))

  /** Destructures ContentCoding. */
  def unapply(coding: ContentCoding): Option[String] =
    Some(coding.name)
}

private case class ContentCodingImpl(name: String) extends ContentCoding
