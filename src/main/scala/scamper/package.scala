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
 * The HTTP library for scala.
 *
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
package object scamper {
  /** Uniform Resource Identifier */
  type Uri = java.net.URI

  /** Provides factory methods for `Uri`. */
  object Uri {
    /** Creates Uri from supplied string. */
    def apply(uri: String): Uri = new Uri(uri)

    /** Creates Uri with supplied scheme, scheme-specific part, and fragment. */
    def create(scheme: String, schemeSpecificPart: String, fragment: String = null): Uri =
      new Uri(scheme, schemeSpecificPart, fragment)

    /** Creates Uri with http scheme and supplied components. */
    def http(authority: String, path: String = null, query: String = null, fragment: String = null): Uri =
      new Uri("http", authority, path, query, fragment)

    /** Creates Uri with https scheme and supplied components. */
    def https(authority: String, path: String = null, query: String = null, fragment: String = null): Uri =
      new Uri("https", authority, path, query, fragment)
  }
}
