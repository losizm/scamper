package scamper.types

import scala.util.Try
import scamper.ListParser

/**
 * Standardized type for Range header value.
 *
 * @see [[scamper.headers.Range]]
 */
trait RangeType {
  /** Range unit */
  def unit: String

  /** Range set */
  def set: Any
}

/**
 * Standardized type for Range header value.
 *
 * @see [[scamper.headers.Range]]
 */
trait ByteRange extends RangeType {
  import ByteRange._

  /** Byte range unit (i.e., "bytes") */
  val unit: String = "bytes"

  /** Byte range set */
  def set: Seq[ByteRangeSpec]

  /** Gets formatted byte range. */
  lazy override val toString: String =
    unit + '=' + (set.map {
      case Slice(first, Some(last)) => first + "-" + last
      case Slice(first, None) => first + "-"
      case Suffix(length) => "-" + length
    } mkString ",")
}

/** ByteRange factory */
object ByteRange {
  private val syntax = """(?i:bytes)\s*=\s*(.+)""".r
  private val slice = """(\d+)-(\d+)?""".r
  private val suffix = """-(\d+)?""".r

  /** Parses formatted byte range. */
  def apply(range: String): ByteRange =
    range match {
      case syntax(set) => new ByteRangeImpl(parseSet(set))
      case _ => throw new IllegalArgumentException(s"Malformed byte range: $range")
    }

  /** Creates ByteRange from supplied specs. */
  def apply(specs: ByteRangeSpec*): ByteRange =
    new ByteRangeImpl(specs)

  /** Destructures ByteRange. */
  def unapply(range: ByteRange): Option[(String, Seq[ByteRangeSpec])] =
    Some((range.unit, range.set))

  private def parseSet(set: String): Seq[ByteRangeSpec] =
    ListParser(set).map {
      case slice(first, null) => Slice(first.toLong, None)
      case slice(first, last) => Slice(first.toLong, Some(last.toLong))
      case suffix(length) => Suffix(length.toLong)
    }

  /**
   * Byte range spec
   *
   * @see [[ByteRange]]
   */
  sealed trait ByteRangeSpec

  /** Byte range spec with first position and optional last position. */
  case class Slice(first: Long, last: Option[Long]) extends ByteRangeSpec 

  /** Byte range spec with suffix length. */
  case class Suffix(length: Long) extends ByteRangeSpec
}

private class ByteRangeImpl(val set: Seq[ByteRange.ByteRangeSpec]) extends ByteRange

