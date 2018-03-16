package scamper.types

import scala.util.Try

/**
 * Standardized type for Content-Range header value.
 *
 * @see [[scamper.ImplicitHeaders.ContentRange]]
 */
trait ContentRangeType {
  /** Range unit */
  def unit: String

  /** Range response */
  def resp: Any
}

/**
 * Standardized type for Content-Range header value.
 *
 * @see [[scamper.ImplicitHeaders.ContentRange]]
 */
trait ByteContentRange extends ContentRangeType {
  import ByteContentRange._

  /** Byte range unit (i.e., "bytes") */
  val unit: String = "bytes"

  /** Byte range response */
  def resp: ByteRangeResp

  /** Gets formatted byte content range. */
  lazy override val toString: String =
    unit + ' ' + (resp match {
      case Satisfied(first, last, length) => s"$first-$last/${length.getOrElse('*')}"
      case Unsatisfied(length) => s"*/$length"
    })
}

/** ByteContentRange factory */
object ByteContentRange {
  private val syntax = """(?i:bytes)\s+(.+)""".r
  private val satisfied = """(\d+)-(\d+)/(\*|\d+)""".r
  private val unsatisfied = """\*/(\d+)""".r

  /** Parses formatted byte content range. */
  def apply(range: String): ByteContentRange =
    range match {
      case syntax(resp) => ByteContentRangeImpl(parseResp(resp))
      case _ => throw new IllegalArgumentException(s"Malformed byte content range: $range")
    }

  /** Creates ByteContentRange from supplied response. */
  def apply(resp: ByteRangeResp): ByteContentRange =
    ByteContentRangeImpl(resp)

  /** Destructures ByteContentRange. */
  def unapply(range: ByteContentRange): Option[(String, ByteRangeResp)] =
    Some((range.unit, range.resp))

  private def parseResp(resp: String): ByteRangeResp =
    resp match {
      case satisfied(first, last, "*")    => Satisfied(first.toLong, last.toLong, None)
      case satisfied(first, last, length) => Satisfied(first.toLong, last.toLong, Some(length.toLong))
      case unsatisfied(length)            => Unsatisfied(length.toLong)
    }

  /**
   * Byte range response
   *
   * @see [[ByteContentRange]]
   */
  sealed trait ByteRangeResp

  /** Satisfied byte range response. */
  case class Satisfied(first: Long, last: Long, completeLength: Option[Long]) extends ByteRangeResp

  /** Unsatisfied byte range response. */
  case class Unsatisfied(completeLength: Long) extends ByteRangeResp
}

private case class ByteContentRangeImpl(resp: ByteContentRange.ByteRangeResp) extends ByteContentRange

