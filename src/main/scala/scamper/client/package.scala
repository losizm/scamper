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
 * Defines HTTP client implementation.
 *
 * ### Using HTTP Client
 *
 * The [[HttpClient$ HttpClient]] object can be used to send a request and handle
 * the response.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.client.HttpClient
 *
 * given BodyParser[String] = BodyParser.text()
 *
 * def getMessageOfTheDay(): Either[Int, String] =
 *   val req = Get("localhost:8080/motd")
 *
 *   // Send request and handle response
 *   HttpClient.send(req) { res =>
 *     res.isSuccessful match
 *       case true  => Right(res.as[String])
 *       case false => Left(res.statusCode)
 *   }
 * }}}
 *
 * Note the request must be created with an absolute URI to make effective use
 * of the client.
 *
 * ### Creating HTTP Client
 *
 * When using the `HttpClient` object as the client, it creates an
 * [[HttpClient]] instance for one-time usage. If you plan to send multiple
 * requests, you can create and maintain a reference to a client. With it, you
 * also get access to methods corresponding to standard HTTP request methods.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.client.HttpClient
 *
 * given BodyParser[String] = BodyParser.text()
 *
 * // Create HttpClient instance
 * val client = HttpClient()
 *
 * def getMessageOfTheDay(): Either[Int, String] =
 *   // Use client instance
 *   client.get("http://localhost:8080/motd") { res =>
 *     res.isSuccessful match
 *       case true  => Right(res.as[String])
 *       case false => Left(res.statusCode)
 *   }
 * }}}
 *
 * And, if a given client is in scope, you can make use of `send()` on the
 * request itself.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.client.{ ClientHttpRequest, HttpClient }
 * import scamper.headers.{ Accept, AcceptLanguage }
 * import scamper.types.{ stringToMediaRange, stringToLanguageRange }
 *
 * given HttpClient = HttpClient()
 * given BodyParser[String] = BodyParser.text(4096)
 *
 * Get("http://localhost:8080/motd")
 *   .setAccept("text/plain")
 *   .setAcceptLanguage("en-US; q=0.6", "fr-CA; q=0.4")
 *   .send(res => println(res.as[String])) // Send request and print response
 * }}}
 *
 * See also [[ClientSettings]] for information about configuring the HTTP
 * client before it is created.
 */
package client

/** Indicates request is aborted. */
case class RequestAborted(message: String) extends HttpException(message)

/** Defines filter for outgoing request. */
@FunctionalInterface
trait RequestFilter:
  /** Filters outgoing request. */
  def apply(req: HttpRequest): HttpRequest

/** Defines handler for incoming response. */
@FunctionalInterface
trait ResponseHandler[T]:
  /** Handles response. */
  def apply(res: HttpResponse): T

/** Defines filter for incoming response. */
@FunctionalInterface
trait ResponseFilter extends ResponseHandler[HttpResponse]
