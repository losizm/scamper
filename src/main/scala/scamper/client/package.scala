/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.io.File

import javax.net.ssl.TrustManager

import scala.util.Try

import cookies.{ PlainCookie, RequestCookies }

import Auxiliary.UriType
import RequestMethod.Registry._

/**
 * Defines HTTP client implementation.
 *
 * === Using HTTP Client ===
 *
 * The [[HttpClient$ HttpClient]] object can be used to send a request and handle
 * the response.
 *
 * {{{
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.client.HttpClient
 *
 * implicit val parser = BodyParser.text()
 *
 * def getMessageOfTheDay(): Either[Int, String] = {
 *   val req = Get("localhost:8080/motd")
 *
 *   // Send request and handle response
 *   HttpClient.send(req) { res =>
 *     res.isSuccessful match {
 *       case true  => Right(res.as[String])
 *       case false => Left(res.statusCode)
 *     }
 *   }
 * }
 * }}}
 *
 * Note the request must be created with an absolute URI to make effective use
 * of the client.
 *
 * === Creating HTTP Client ===
 *
 * When using the `HttpClient` object as the client, it creates an
 * [[HttpClient]] instance for one-time usage. If you plan to send multiple
 * requests, you can create and maintain a reference to a client. With it, you
 * also get access to methods corresponding to standard HTTP request methods.
 *
 * {{{
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.client.HttpClient
 *
 * implicit val parser = BodyParser.text()
 *
 * // Create HttpClient instance
 * val client = HttpClient()
 *
 * def getMessageOfTheDay(): Either[Int, String] = {
 *   // Use client instance
 *   client.get("http://localhost:8080/motd") { res =>
 *     res.isSuccessful match {
 *       case true  => Right(res.as[String])
 *       case false => Left(res.statusCode)
 *     }
 *   }
 * }
 * }}}
 *
 * And, if an implicit client is in scope, you can make use of `send()` on the
 * request itself.
 *
 * {{{
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.Get
 * import scamper.client.HttpClient
 * import scamper.client.Implicits.ClientHttpRequestType // Adds send method to request
 * import scamper.headers.{ Accept, AcceptLanguage }
 * import scamper.types.Implicits.{ stringToMediaRange, stringToLanguageRange }
 *
 * implicit val client = HttpClient()
 * implicit val parser = BodyParser.text(4096)
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
package object client {
  /** Indicates request is aborted. */
  case class RequestAborted(message: String) extends HttpException(message)

  /** Defines filter for outgoing request. */
  trait RequestFilter {
    /** Filters outgoing request. */
    def apply(req: HttpRequest): HttpRequest
  }

  /** Defines handler for incoming response. */
  trait ResponseHandler[T] {
    /** Handles response. */
    def apply(res: HttpResponse): T
  }

  /** Defines filter for incoming response. */
  trait ResponseFilter extends ResponseHandler[HttpResponse]
}
