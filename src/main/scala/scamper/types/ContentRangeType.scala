/*
 * Copyright 2018 Carlos Conyers
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

/**
 * Standardized type for Content-Range header value.
 *
 * @see [[scamper.ImplicitHeaders.ContentRange]]
 */
trait ContentRangeType {
  /** Gets range unit. */
  def unit: String

  /** Gets range response. */
  def resp: Any
}

/**
 * Standardized type for Content-Range header value.
 *
 * @see [[scamper.ImplicitHeaders.ContentRange]]
 */
trait ByteContentRange extends ContentRangeType {
  import ByteContentRange._

  /** Gets byte range unit (i.e., "bytes"). */
  val unit: String = "bytes"

  /** Gets byte range response. */
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
  def parse(range: String): ByteContentRange =
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
