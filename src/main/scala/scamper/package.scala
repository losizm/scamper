/*
 * Copyright 2019 Carlos Conyers
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

/**
 * === HTTP Messages ===
 *
 * At the core ''Scamper'' is [[HttpMessage]], which is a trait that defines the
 * fundamental characteristics of an HTTP message. [[HttpRequest]] and
 * [[HttpResponse]] extend the specification to define characteristics specific
 * to their respective message types.
 *
 * An [[HttpRequest]] is created using one of the factory methods defined in its
 * companion object. Or you can start with a [[RequestMethod]] and use builder
 * methods to further define the request.
 *
 * {{{
 * import scamper.Header
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.GET
 *
 * val request = GET("/motd").withHeaders(
 *   Header("Host", "localhost:8080"),
 *   Header("Accept", "text/plain")
 * )
 *
 * printf("Request Method: %s%n", request.method)
 * printf("Target URI: %s%n", request.target)
 *
 * request.headers.foreach(println)
 *
 * val host: Option[String] = request.getHeaderValue("Host")
 * }}}
 *
 * An [[HttpResponse]] is created using one of the factory methods defined in
 * its companion object. Or you can start with a [[ResponseStatus]] and use
 * builder methods to further define the response.
 *
 * {{{
 * import scamper.{ BodyParsers, Header }
 * import scamper.Implicits.stringToEntity
 * import scamper.ResponseStatus.Registry.Ok
 *
 * val response = Ok("There is an answer.").withHeaders(
 *   Header("Content-Type", "text/plain"),
 *   Header("Connection", "close")
 * )
 *
 * printf("Status Code: %s%n", response.status.code)
 * printf("Reason Phrase: %s%n", response.status.reason)
 *
 * response.headers.foreach(println)
 *
 * val contentType: Option[String] = response.getHeaderValue("Content-Type")
 *
 * implicit val parser = BodyParsers.text()
 *
 * printf("Body: %s%n", response.as[String])
 * }}}
 */
package object scamper
