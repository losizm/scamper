package scamper

import java.io.File
import java.time.OffsetDateTime

/** Contains implicit conversion functions. */
object ImplicitConversions {
  /** Converts string to [[Header]]. */
  implicit val stringToHeader = (header: String) => Header(header)

  /** Converts tuple to [[Header]] where tuple is key-value pair. */
  implicit val tupleToHeader = (header: (String, String)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is key-value pair. */
  implicit val tupleToHeaderWithLongValue = (header: (String, Long)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is key-value pair. */
  implicit val tupleToHeaderWithIntValue = (header: (String, Int)) => Header(header._1, header._2)

  /** Converts tuple to [[Header]] where tuple is key-value pair. */
  implicit val tupleToHeaderWithDateValue = (header: (String, OffsetDateTime)) => Header(header._1, header._2)

  /** Converts byte array to [[Entity]]. */
  implicit val bytesToEntity = (entity: Array[Byte]) => Entity(entity)

  /** Converts string to [[Entity]] where text is UTF-8 encoded. */
  implicit val stringToEntity = (entity: String) => Entity(entity, "UTF-8")

  /**
   * Converts tuple to [[Entity]] where tuple is text and character encoding.
   */
  implicit val tupleToEntity = (entity: (String, String)) => Entity(entity._1, entity._2)

  /** Converts file to [[Entity]]. */
  implicit val fileToEntity = (entity: File) => Entity(entity)

  /** Converts string to [[MediaType]]. */
  implicit val stringToMediaType = (value: String) => MediaType(value)

  /** Converts string to [[MediaRange]]. */
  implicit val stringToMediaRange = (value: String) => MediaRange(value)
}

