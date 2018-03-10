package scamper.types

import CodingHelper._

/**
 * Standardized type for Transfer-Encoding header value.
 *
 * @see [[scamper.headers.TransferEncoding]]
 */
trait TransferCoding {
  /** Coding name */
  def name: String

  /** Coding parameters */
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
  def apply(coding: String): TransferCoding =
    ParseTransferCoding(coding) match {
      case (name, params) => apply(name, params)
    }

  /** Creates TransferCoding with supplied values. */
  def apply(name: String, params: Map[String, String]): TransferCoding =
    new TransferCodingImpl(Name(name), Params(params))

  /** Creates TransferCoding with supplied values. */
  def apply(name: String, params: (String, String)*): TransferCoding =
    apply(name, params.toMap)

  /** Destructures TransferCoding. */
  def unapply(coding: TransferCoding): Option[(String, Map[String, String])] =
    Some((coding.name, coding.params))
}

private class TransferCodingImpl(val name: String, val params: Map[String, String]) extends TransferCoding

