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

  /** Converts string to [[LinkValue]]. */
  implicit val stringToLinkValue = (link: String) => LinkValue.parse(link)

  /** Converts string to [[MediaType]]. */
  implicit val stringToMediaType = (mediaType: String) => MediaType(mediaType)

  /** Converts string to [[MediaRange]]. */
  implicit val stringToMediaRange = (range: String) => MediaRange(range)

  /** Converts string to [[PragmaDirective]]. */
  implicit val stringToPragmaDirective = (directive: String) => PragmaDirective.parse(directive)

  /** Converts string to [[ProductType]]. */
  implicit val stringToProductType = (product: String) => ProductType(product)

  /** Converts string to [[Protocol]]. */
  implicit val stringToProtocol = (protocol: String) => Protocol(protocol)

  /** Converts string to [[TransferCoding]]. */
  implicit val stringToTransferCoding = (coding: String) => TransferCoding(coding)

  /** Converts string to [[TransferCodingRange]]. */
  implicit val stringToTransferCodingRange = (range: String) => TransferCodingRange(range)

  /** Converts string to [[ViaType]]. */
  implicit val stringToViaType = (via: String) => ViaType.parse(via)

  /** Converts string to [[WarningType]]. */
  implicit val stringToWarningType = (warning: String) => WarningType.parse(warning)
}
