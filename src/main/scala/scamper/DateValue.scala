package scamper

import java.time.{ OffsetDateTime, ZoneOffset }
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => DateFormatter }

private object DateValue {
  private val z = ZoneOffset.of("Z")

  def format(value: OffsetDateTime): String =
    DateFormatter.format(value.atZoneSameInstant(z))

  def parse(value: String): OffsetDateTime =
    OffsetDateTime.parse(value, DateFormatter)
}

