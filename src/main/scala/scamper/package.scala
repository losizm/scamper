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

/**
 * Defines core types.
 *
 * ### HTTP Messages
 *
 * At the core of _Scamper_ is [[HttpMessage]], which is a trait that defines
 * the fundamental characteristics of an HTTP message. [[HttpRequest]] and
 * [[HttpResponse]] extend the specification to define characteristics specific
 * to their respective message types.
 *
 * An [[HttpRequest]] is created using a factory method defined in its companion
 * object. Or you can start with a [[RequestMethod]] and use builder methods to
 * further define the request.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.{ Header, stringToUri }
 * import scamper.RequestMethod.Registry.Get
 *
 * val request = Get("/motd").setHeaders(
 *   Header("Host: localhost:8080"),
 *   Header("Accept: text/plain")
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
 * An [[HttpResponse]] is created using a factory method defined in its
 * companion object. Or you can start with a [[ResponseStatus]] and use builder
 * methods to further define the response.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.{ BodyParser, Header, stringToEntity }
 * import scamper.ResponseStatus.Registry.Ok
 *
 * val response = Ok("There is an answer.").setHeaders(
 *   Header("Content-Type: text/plain"),
 *   Header("Connection: close")
 * )
 *
 * printf("Status Code: %s%n", response.statusCode)
 * printf("Reason Phrase: %s%n", response.reasonPhrase)
 *
 * response.headers.foreach(println)
 *
 * val contentType: Option[String] = response.getHeaderValue("Content-Type")
 *
 * given BodyParser[String] = BodyParser.text()
 *
 * printf("Body: %s%n", response.as[String])
 * }}}
 */
package scamper

/** Defines alias to `java.net.URI`. */
type Uri = java.net.URI

/** Provides factory for `Uri`. */
object Uri:
  /** Creates normalized URI with supplied string. */
  def apply(uri: String): Uri =
    new Uri(uri).normalize()

  /**
   * Creates normalized URI with supplied scheme, scheme-specific part, and
   * fragment.
   */
  def apply(scheme: String, schemeSpecificPart: String, fragment: String = null): Uri =
    new Uri(scheme, schemeSpecificPart, fragment).normalize()
