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

/**
 * Defines standard types for header values.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.Implicits.{ stringToEntity, stringToUri }
 * import scamper.RequestMethod.Registry.Get
 * import scamper.ResponseStatus.Registry.Ok
 * import scamper.headers.{ Accept, ContentType, TransferEncoding }
 * import scamper.types.{ MediaRange, MediaType, TransferCoding }
 *
 * val json = MediaRange("application", "json", 0.9f)
 * val html = MediaRange("text/html; q=0.1")
 *
 * val req = Get("/motd")
 *   .setAccept(json, html)
 *
 * val text = MediaType("text/plain")
 * val gzip = TransferCoding("gzip")
 *
 * val res = Ok("There is an answer.")
 *   .setContentType(text)
 *   .setTransferEncoding(gzip)
 * }}}
 *
 * Using values defined in [[Implicits]], properly formatted strings
 * can be implicitly converted to standardized types.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.Implicits.{ stringToEntity, stringToUri }
 * import scamper.RequestMethod.Registry.Get
 * import scamper.ResponseStatus.Registry.Ok
 * import scamper.headers.{ Accept, ContentType, TransferEncoding }
 * import scamper.types.{ *, given }
 *
 * val req = Get("/motd")
 *   .setAccept("application/json; q=0.9", "text/html; q=0.1")
 *
 * val res = Ok("There is an answer.")
 *   .setContentType("text/plain")
 *   .setTransferEncoding("gzip")
 * }}}
 *
 * @see [[scamper.headers]]
 */
package object types
