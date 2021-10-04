/*
 * Copyright 2021 Carlos Conyers
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
package scamper
package http
package types

/** Converts string to [[ByteRange]]. */
given stringToByteRange: Conversion[String, ByteRange] with
  def apply(range: String) = ByteRange.parse(range)

/** Converts string to [[ByteContentRange]]. */
given stringToByteContentRange: Conversion[String, ByteContentRange] with
  def apply(range: String) = ByteContentRange.parse(range)

/** Converts string to [[CacheDirective]]. */
given stringToCacheDirective: Conversion[String, CacheDirective] with
  def apply(directive: String) = CacheDirective.parse(directive)

/** Converts string to [[CharsetRange]]. */
given stringToCharsetRange: Conversion[String, CharsetRange] with
  def apply(range: String) = CharsetRange.parse(range)

/** Converts string to [[ContentCoding]]. */
given stringToContentCoding: Conversion[String, ContentCoding] with
  def apply(coding: String) = ContentCoding(coding)

/** Converts string to [[ContentCodingRange]]. */
given stringToContentCodingRange: Conversion[String, ContentCodingRange] with
  def apply(range: String) = ContentCodingRange.parse(range)

/** Converts string to [[DispositionType]]. */
given stringToDispositionType: Conversion[String, DispositionType] with
  def apply(disposition: String) = DispositionType.parse(disposition)

/** Converts string to [[EntityTag]]. */
given stringToEntityTag: Conversion[String, EntityTag] with
  def apply(tag: String) = EntityTag.parse(tag)

/** Converts string to [[KeepAliveParameters]]. */
given stringToKeepAliveParameters: Conversion[String, KeepAliveParameters] with
  def apply(params: String) = KeepAliveParameters.parse(params)

/** Converts string to [[LanguageTag]]. */
given stringToLanguageTag: Conversion[String, LanguageTag] with
  def apply(tag: String) = LanguageTag.parse(tag)

/** Converts string to [[LanguageRange]]. */
given stringToLanguageRange: Conversion[String, LanguageRange] with
  def apply(range: String) = LanguageRange.parse(range)

/** Converts string to [[LinkType]]. */
given stringToLinkType: Conversion[String, LinkType] with
  def apply(link: String) = LinkType.parse(link)

/** Converts string to [[MediaType]]. */
given stringToMediaType: Conversion[String, MediaType] with
  def apply(mediaType: String) = MediaType(mediaType)

/** Converts string to [[MediaRange]]. */
given stringToMediaRange: Conversion[String, MediaRange] with
  def apply(range: String) = MediaRange(range)

/** Converts string to [[PragmaDirective]]. */
given stringToPragmaDirective: Conversion[String, PragmaDirective] with
  def apply(directive: String) = PragmaDirective.parse(directive)

/** Converts string to [[Preference]]. */
given stringToPreference: Conversion[String, Preference] with
  def apply(preference: String) = Preference.parse(preference)

/** Converts string to [[ProductType]]. */
given stringToProductType: Conversion[String, ProductType] with
  def apply(product: String) = ProductType.parse(product)

/** Converts string to [[Protocol]]. */
given stringToProtocol: Conversion[String, Protocol] with
  def apply(protocol: String) = Protocol.parse(protocol)

/** Converts string to [[TransferCoding]]. */
given stringToTransferCoding: Conversion[String, TransferCoding] with
  def apply(coding: String) = TransferCoding.parse(coding)

/** Converts string to [[TransferCodingRange]]. */
given stringToTransferCodingRange: Conversion[String, TransferCodingRange] with
  def apply(range: String) = TransferCodingRange.parse(range)

/** Converts string to [[ViaType]]. */
given stringToViaType: Conversion[String, ViaType] with
  def apply(via: String) = ViaType.parse(via)

/** Converts string to [[WarningType]]. */
given stringToWarningType: Conversion[String, WarningType] with
  def apply(warning: String) = WarningType.parse(warning)
