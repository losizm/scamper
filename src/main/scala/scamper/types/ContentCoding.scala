package scamper.types

import CodingHelper.Name

/**
 * Standardized type for Content-Encoding header value.
 *
 * @see [[scamper.ImplicitHeaders.ContentEncoding]]
 */
trait ContentCoding {
  /** Coding name */
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

