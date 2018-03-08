package scamper.types

/** Contains implicit converter functions. */
object ImplicitConverters {
  /** Converts string to [[ContentCoding]]. */
  implicit val stringToContentCoding = (coding: String) => ContentCoding(coding)

  /** Converts string to [[ContentCodingRange]]. */
  implicit val stringToContentCodingRange = (range: String) => ContentCodingRange(range)

  /** Converts string to [[MediaType]]. */
  implicit val stringToMediaType = (mediaType: String) => MediaType(mediaType)

  /** Converts string to [[MediaRange]]. */
  implicit val stringToMediaRange = (range: String) => MediaRange(range)

  /** Converts string to [[TransferCoding]]. */
  implicit val stringToTransferCoding = (coding: String) => TransferCoding(coding)

  /** Converts string to [[TransferCodingRange]]. */
  implicit val stringToTransferCodingRange = (range: String) => TransferCodingRange(range)

  /** Converts string to [[ByteRange]]. */
  implicit val stringToByteRange = (range: String) => ByteRange(range)

  /** Converts string to [[ByteContentRange]]. */
  implicit val stringToByteContentRange = (range: String) => ByteContentRange(range)
}

