package scamper

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => dateFormatter }

import HeaderHelper._

/** HTTP header */
trait Header {
  /** Header key */
  def key: String

  /** Header value */
  def value: String

  /** Gets header value as OffsetDateTime. */
  def dateValue: OffsetDateTime =
    OffsetDateTime.parse(value, dateFormatter)

  /** Gets header value as Long. */
  def longValue: Long = value.toLong

  /** Returns formatted HTTP header. */
  override lazy val toString: String = s"$key: $value"
}

/** Header factory */
object Header {
  /** Creates Header using supplied key and value. */
  def apply(key: String, value: String): Header =
    HeaderImpl(Key(key), Value(value))

  /** Creates Header using supplied key and value. */
  def apply(key: String, value: Long): Header =
    apply(key, value.toString)

  /** Creates Header using supplied key and value. */
  def apply(key: String, value: OffsetDateTime): Header =
    apply(key, dateFormatter.format(value))

  /** Parses formatted header. */
  def apply(header: String): Header =
    header.split(":", 2) match {
      case Array(key, value) => apply(key.trim, value.trim)
      case _ => throw new IllegalArgumentException(s"Malformed header: $header")
    }

  /** Destructures Header to key-value pair. */
  def unapply(header: Header): Option[(String, String)] =
    Some(header.key -> header.value)
}

private case class HeaderImpl(key: String, value: String) extends Header

