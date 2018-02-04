package scamper

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => dateFormatter }

/** Provides key-value pair of HTTP header. */
case class Header private(key: String, value: String) {
  /** Returns formatted HTTP header. */
  override val toString: String = s"$key: $value"

  /** Gets header value as OffsetDateTime. */
  def dateValue: OffsetDateTime =
    OffsetDateTime.parse(value, dateFormatter)

  /** Gets header value as Long. */
  def longValue: Long = value.toLong
}

/** Provides Header factory methods. */
object Header {
  private val HeaderRegex = s"(${Token.regex}):\\s*(.*)\\s*".r

  /** Creates Header using supplied key and value. */
  def apply(key: String, value: String): Header = {
    if (!Token(key))
      throw new IllegalArgumentException(s"Invalid header key: $key")

    new Header(key, value)
  }

  /** Creates Header using supplied key and value. */
  def apply(key: String, value: Long): Header = {
    if (!Token(key))
      throw new IllegalArgumentException(s"Invalid header key: $key")

    new Header(key, value.toString)
  }

  /** Creates Header using supplied key and value. */
  def apply(key: String, value: OffsetDateTime): Header = {
    if (!Token(key))
      throw new IllegalArgumentException(s"Invalid header key: $key")

    new Header(key, dateFormatter.format(value))
  }

  /** Parses formatted header. */
  def apply(header: String): Header =
    header match {
      case HeaderRegex(key, value) => new Header(key, value)
      case _ => throw new IllegalArgumentException(s"Malformed header: $header")
    }
}

