package scamper

import TransferCodingHelper._

case class TransferCoding(name: String, params: Map[String, String] = Map.empty) {
  Name(name)
  Params(params)

  /** Tests whether name is chunked. */
  def isChunked: Boolean = name.equalsIgnoreCase("chunked")

  /** Tests whether name is compress. */
  def isCompress: Boolean = name.equalsIgnoreCase("compress")

  /** Tests whether name is deflate. */
  def isDeflate: Boolean = name.equalsIgnoreCase("deflate")

  /** Tests whether name is gzip. */
  def isGzip: Boolean = name.equalsIgnoreCase("gzip")
}

