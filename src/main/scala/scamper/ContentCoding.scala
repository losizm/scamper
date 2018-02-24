package scamper

import ContentCodingHelper._

case class ContentCoding(name: String, qvalue: Float = 1.0f) {
  Name(name)
  QValue(qvalue)

  /** Tests whether name is compress. */
  def isCompress: Boolean = name.equalsIgnoreCase("compress")

  /** Tests whether name is deflate. */
  def isDeflate: Boolean = name.equalsIgnoreCase("deflate")

  /** Tests whether name is gzip. */
  def isGzip: Boolean = name.equalsIgnoreCase("gzip")

  /** Tests whether name is gzip. */
  def isIdentity: Boolean = name.equalsIgnoreCase("identity")
}

