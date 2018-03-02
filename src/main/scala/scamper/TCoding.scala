package scamper

import TransferCodingHelper._

/** T-Coding */
trait TCoding {
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
  
  /** Returns formatted t-coding. */
  override lazy val toString: String = {
    val tcoding = new StringBuilder
    tcoding.append(name)
    if (isTrailers || rank < 1f) tcoding.append("; q=").append(rank)
    if (params.nonEmpty) tcoding.append(FormatParams(params))
    tcoding.toString
  }
}

/** TCoding factory */
object TCoding {
  private val qkeyRegex = "([Qq])".r
  private val qvalueRegex = """(\d+(?:\.\d*))""".r

  /** Creates TCoding using supplied values. */
  def apply(name: String, rank: Float = 1.0f, params: Map[String, String] = Map.empty): TCoding =
    new TCodingImpl(Name(name), Qvalue(rank), Params(params))

  /** Parses formatted t-coding. */
  def apply(tcoding: String): TCoding =
    ParseTransferCoding(tcoding) match {
      case (name, params) =>
        params.collectFirst {
          case (qkeyRegex(key), qvalueRegex(value)) => (value.toFloat, (params - key))
        } map {
          case (rank, params) => new TCodingImpl(Name(name), Qvalue(rank), Params(params))
        } getOrElse {
          new TCodingImpl(Name(name), 1.0f, Params(params))
        }
    }

  /** Destructures TCoding. */
  def unapply(tcoding: TCoding): Option[(String, Float, Map[String, String])] =
    Some((tcoding.name, tcoding.rank, tcoding.params))

  private def Qvalue(qvalue: Float): Float =
    (qvalue.max(0f).min(1f) * 1000).floor / 1000
}

private class TCodingImpl(val name: String, val rank: Float, val params: Map[String, String]) extends TCoding

