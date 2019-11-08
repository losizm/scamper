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
package scamper

import java.io.File

import javax.net.ssl.TrustManager

import scala.util.Try

import cookies.{ PlainCookie, RequestCookies }

import Auxiliary.UriType
import RequestMethod.Registry._

/**
 * Provides HTTP client implementation.
 *
 * === Using HTTP Client ===
 *
 * The `HttpClient` object can be used to send a request and handle the
 * response.
 *
 * {{{
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.GET
 * import scamper.client.HttpClient
 *
 * implicit val parser = BodyParser.text()
 *
 * def getMessageOfTheDay(): Either[Int, String] = {
 *   val req = GET("localhost:8080/motd")
 *
 *   // Send request and handle response
 *   HttpClient.send(req) { res =>
 *     res.status.isSuccessful match {
 *       case true  => Right(res.as[String])
 *       case false => Left(res.status.code)
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
 * When using the `HttpClient` object as the client, it creates an instance of
 * `HttpClient` for one-time usage. If you plan to send multiple requests, you
 * can create and maintain a reference to an HttpClient instance. With that,
 * you also get access to methods corresponding to the standard HTTP request
 * methods.
 *
 * {{{
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.client.HttpClient
 *
 * implicit val parser = BodyParser.text()
 *
 * // Create HttpClient instance
 * val client = HttpClient(bufferSize = 4096, readTimeout = 3000)
 *
 * def getMessageOfTheDay(): Either[Int, String] = {
 *   // Use client instance
 *   client.get("http://localhost:8080/motd") { res =>
 *     res.status.isSuccessful match {
 *       case true  => Right(res.as[String])
 *       case false => Left(res.status.code)
 *     }
 *   }
 * }
 * }}}
 *
 * And if the client is declared as an implicit value, you can make use of `send()`
 * on the request itself.
 *
 * {{{
 * import scamper.BodyParser
 * import scamper.Implicits.stringToUri
 * import scamper.RequestMethod.Registry.GET
 * import scamper.client.HttpClient
 * import scamper.client.Implicits.ClientHttpRequestType // Adds send method to request
 * import scamper.headers.{ Accept, AcceptLanguage }
 * import scamper.types.Implicits.{ stringToMediaRange, stringToLanguageRange }
 *
 * implicit val client = HttpClient(bufferSize = 8192, readTimeout = 1000)
 * implicit val parser = BodyParser.text(4096)
 *
 * GET("http://localhost:8080/motd")
 *   .withAccept("text/plain")
 *   .withAcceptLanguage("en-US; q=0.6", "fr-CA; q=0.4")
 *   .send(res => println(res.as[String])) // Send request and print response
 * }}}
 */
package object client {
  /** Indicates request was aborted. */
  case class RequestAborted(message: String) extends HttpException(message)

  /** Provides utility for handling response. */
  trait ResponseHandler[T] {
    /**
     * Handles response.
     *
     * @param response incoming response
     */
    def apply(response: HttpResponse): T
  }

  /** Provides utility for filtering HTTP response. */
  trait ResponseFilter extends ResponseHandler[Boolean] {
    /** Tests whether response matches filter condition. */
    def apply(response: HttpResponse): Boolean

    /**
     * Returns `Some(response)` if response matches filter condition, and `None`
     * otherwise.
     */
    def unapply(response: HttpResponse): Option[HttpResponse] =
      if (apply(response)) Some(response) else None
  }

  /** Includes status-based `ResponseFilter` implementations. */
  object ResponseFilter {
    /**
     * Filters informational responses.
     *
     * See [[ResponseStatus.isInformational]].
     */
    val Informational: ResponseFilter =
      res => res.status.isInformational

    /**
     * Filters successful responses.
     *
     * See [[ResponseStatus.isSuccessful]].
     */
    val Successful: ResponseFilter =
      res => res.status.isSuccessful

    /**
     * Filters redirection responses.
     *
     * See [[ResponseStatus.isRedirection]].
     */
    val Redirection: ResponseFilter =
      res => res.status.isRedirection

    /**
     * Filters client error responses.
     *
     * See [[ResponseStatus.isClientError]].
     */
    val ClientError: ResponseFilter =
      res => res.status.isClientError

    /**
     * Filters server error responses.
     *
     * See [[ResponseStatus.isServerError]].
     */
    val ServerError: ResponseFilter =
      res => res.status.isServerError
  }
}
