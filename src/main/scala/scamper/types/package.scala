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
package scamper

/**
 * Defines standardized types for header classes in [[scamper.headers]].
 *
 * {{{
 * import scamper.ImplicitConverters.{ stringToEntity, stringToUri }
 * import scamper.RequestMethods.GET
 * import scamper.ResponseStatuses.Ok
 * import scamper.headers.{ Accept, ContentType, TransferEncoding }
 * import scamper.types.{ MediaRange, MediaType, TransferCoding }
 *
 * val json = MediaRange("application", "json", 0.9f)
 * val html = MediaRange.parse("text/html; q=0.1")
 *
 * val req = GET("/motd").withAccept(json, html)
 *
 * val text = MediaType.parse("text/plain")
 * val gzip = TransferCoding("gzip")
 *
 * val res = Ok("There is an answer.").withContentType(text).withTransferEncoding(gzip)
 * }}}
 *
 * Using values defined in [[ImplicitConverters]], properly formatted strings
 * can be implicitly converted to standardized types.
 *
 * {{{
 * import scamper.ImplicitConverters.{ stringToEntity, stringToUri }
 * import scamper.RequestMethods.GET
 * import scamper.ResponseStatuses.Ok
 * import scamper.headers.{ Accept, ContentType, TransferEncoding }
 * import scamper.types.ImplicitConverters._
 *
 * val req = GET("/motd").withAccept("application/json; q=0.9", "text/html; q=0.1")
 *
 * val res = Ok("There is an answer.").withContentType("text/plain").withTransferEncoding("gzip")
 * }}}
 */
package object types

