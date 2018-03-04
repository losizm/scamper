package scamper

import TransferCodingHelper._

/**
 * Transfer Coding Range
 *
 * @see [[ImplicitHeaders.TE]]
 */
trait TransferCodingRange {
  /** Coding name */
  def name: String

  /** Gets coding rank. */
  def rank: Float

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
  
  /** Tests whether name is {@code trailers}. */
  def isTrailers: Boolean = name == "trailers"
  
  /** Returns formatted transfer coding range. */
  override lazy val toString: String = {
    val range = new StringBuilder
    range.append(name)
    if (rank < 1f) range.append("; q=").append(rank)
    if (params.nonEmpty) range.append(FormatParams(params))
    range.toString
  }
}

/** TransferCodingRange factory */
object TransferCodingRange {
  /** Parses formatted transfer coding range. */
  def apply(range: String): TransferCodingRange =
    ParseTransferCoding(range) match {
      case (name, params) =>
        params.collectFirst {
          case (QValue.key(key), QValue.value(value)) => (value.toFloat, (params - key))
        } map {
          case (rank, params) => new TransferCodingRangeImpl(Name(name), QValue(rank), Params(params))
        } getOrElse {
          new TransferCodingRangeImpl(Name(name), 1.0f, Params(params))
        }
    }

  /** Creates TransferCodingRange with supplied values. */
  def apply(name: String, rank: Float = 1.0f, params: Map[String, String] = Map.empty): TransferCodingRange =
    new TransferCodingRangeImpl(Name(name), QValue(rank), Params(params))

  /** Destructures TransferCodingRange. */
  def unapply(range: TransferCodingRange): Option[(String, Float, Map[String, String])] =
    Some((range.name, range.rank, range.params))
}

private class TransferCodingRangeImpl(val name: String, val rank: Float, val params: Map[String, String]) extends TransferCodingRange

