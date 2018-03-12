package scamper.types

/** Contains implicit converter functions. */
object ImplicitConverters {
  /** Converts string to [[ByteRange]]. */
  implicit val stringToByteRange = (range: String) => ByteRange(range)

  /** Converts string to [[ByteContentRange]]. */
  implicit val stringToByteContentRange = (range: String) => ByteContentRange(range)

  /** Converts string to [[CacheDirective]]. */
  implicit val stringToCacheDirective = (directive: String) => CacheDirective.parse(directive)

  /** Converts string to [[Challenge]]. */
  implicit val stringToChallenge = (challenge: String) => Challenge.parse(challenge)

  /** Converts string to [[Credentials]]. */
  implicit val stringToCredentials = (credentials: String) => Credentials.parse(credentials)

  /** Converts string to [[CharsetRange]]. */
  implicit val stringToCharsetRange = (range: String) => CharsetRange(range)

  /** Converts string to [[ContentCoding]]. */
  implicit val stringToContentCoding = (coding: String) => ContentCoding(coding)

  /** Converts string to [[ContentCodingRange]]. */
  implicit val stringToContentCodingRange = (range: String) => ContentCodingRange(range)

  /** Converts string to [[ContentDispositionType]]. */
  implicit val stringToContentDispositionType = (disposition: String) => ContentDispositionType(disposition)

  /** Converts string to [[EntityTag]]. */
  implicit val stringToEntityTag = (tag: String) => EntityTag(tag)

  /** Converts string to [[LanguageTag]]. */
  implicit val stringToLanguageTag = (tag: String) => LanguageTag(tag)

  /** Converts string to [[LanguageRange]]. */
  implicit val stringToLanguageRange = (range: String) => LanguageRange(range)

  /** Converts string to [[MediaType]]. */
  implicit val stringToMediaType = (mediaType: String) => MediaType(mediaType)

  /** Converts string to [[MediaRange]]. */
  implicit val stringToMediaRange = (range: String) => MediaRange(range)

  /** Converts string to [[ProductType]]. */
  implicit val stringToProductType = (product: String) => ProductType(product)

  /** Converts string to [[TransferCoding]]. */
  implicit val stringToTransferCoding = (coding: String) => TransferCoding(coding)

  /** Converts string to [[TransferCodingRange]]. */
  implicit val stringToTransferCodingRange = (range: String) => TransferCodingRange(range)

  /** Converts string to [[WarningType]]. */
  implicit val stringToWarningType = (warning: String) => WarningType.parse(warning)
}

