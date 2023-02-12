/*
 * Copyright 2023 Carlos Conyers
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
given stringToByteRange: Conversion[String, ByteRange] = ByteRange.parse(_)

/** Converts string to [[ByteContentRange]]. */
given stringToByteContentRange: Conversion[String, ByteContentRange] = ByteContentRange.parse(_)

/** Converts string to [[CacheDirective]]. */
given stringToCacheDirective: Conversion[String, CacheDirective] = CacheDirective.parse(_)

/** Converts string to [[CharsetRange]]. */
given stringToCharsetRange: Conversion[String, CharsetRange] = CharsetRange.parse(_)

/** Converts string to [[ContentCoding]]. */
given stringToContentCoding: Conversion[String, ContentCoding] = ContentCoding(_)

/** Converts string to [[ContentCodingRange]]. */
given stringToContentCodingRange: Conversion[String, ContentCodingRange] = ContentCodingRange.parse(_)

/** Converts string to [[DispositionType]]. */
given stringToDispositionType: Conversion[String, DispositionType] = DispositionType.parse(_)

/** Converts string to [[EntityTag]]. */
given stringToEntityTag: Conversion[String, EntityTag] = EntityTag.parse(_)

/** Converts string to [[KeepAliveParameters]]. */
given stringToKeepAliveParameters: Conversion[String, KeepAliveParameters] = KeepAliveParameters.parse(_)

/** Converts string to [[LanguageTag]]. */
given stringToLanguageTag: Conversion[String, LanguageTag] = LanguageTag.parse(_)

/** Converts string to [[LanguageRange]]. */
given stringToLanguageRange: Conversion[String, LanguageRange] = LanguageRange.parse(_)

/** Converts string to [[LinkType]]. */
given stringToLinkType: Conversion[String, LinkType] = LinkType.parse(_)

/** Converts string to [[MediaType]]. */
given stringToMediaType: Conversion[String, MediaType] = MediaType(_)

/** Converts string to [[MediaRange]]. */
given stringToMediaRange: Conversion[String, MediaRange] = MediaRange(_)

/** Converts string to [[PragmaDirective]]. */
given stringToPragmaDirective: Conversion[String, PragmaDirective] = PragmaDirective.parse(_)

/** Converts string to [[Preference]]. */
given stringToPreference: Conversion[String, Preference] = Preference.parse(_)

/** Converts string to [[ProductType]]. */
given stringToProductType: Conversion[String, ProductType] = ProductType.parse(_)

/** Converts string to [[Protocol]]. */
given stringToProtocol: Conversion[String, Protocol] = Protocol.parse(_)

/** Converts string to [[TransferCoding]]. */
given stringToTransferCoding: Conversion[String, TransferCoding] = TransferCoding.parse(_)

/** Converts string to [[TransferCodingRange]]. */
given stringToTransferCodingRange: Conversion[String, TransferCodingRange] = TransferCodingRange.parse(_)

/** Converts string to [[ViaType]]. */
given stringToViaType: Conversion[String, ViaType] = ViaType.parse(_)

/** Converts string to [[WarningType]]. */
given stringToWarningType: Conversion[String, WarningType] = WarningType.parse(_)
