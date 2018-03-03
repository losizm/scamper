package scamper

/** Content Coding */
trait ContentCoding {
  /** Coding name */
  def name: String

  /** Tests whether name is compress (or x-compress). */
  def isCompress: Boolean = name == "compress" || name == "x-compress"

  /** Tests whether name is deflate. */
  def isDeflate: Boolean = name == "deflate"

  /** Tests whether name is gzip (or x-gzip). */
  def isGzip: Boolean = name == "gzip" || name == "x-gzip"

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
    if (name.matches("(?i:(x-)?compress|deflate|(x-)?gzip|identity)")) new ContentCodingImpl(name.toLowerCase)
    else throw new IllegalArgumentException(s"Invalid content coding name: $name")

  /** Destructures ContentCoding. */
  def unapply(coding: ContentCoding): Option[String] =
    Some(coding.name)
}

private class ContentCodingImpl(val name: String) extends ContentCoding

