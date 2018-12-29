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

import java.io.File
import java.net.URI

import aux.UriType
import cookies.{ PlainCookie, RequestCookies }

import RequestMethods._

/**
 * Provides HTTP client implementation.
 *
 * === Using HTTP Client ===
 *
 * The `HttpClient` object can be used to send a request and handle the
 * response.
 *
 * {{{
 * import scamper.BodyParsers
 * import scamper.ImplicitConverters.stringToUri
 * import scamper.RequestMethods.GET
 * import scamper.client.HttpClient
 * import scamper.headers.Host
 *
 * implicit val parser = BodyParsers.text()
 *
 * def getMessageOfTheDay(): Either[Int, String] = {
 *   val req = GET("/motd").withHost("localhost:8080")
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
 * === Creating HTTP Client ===
 *
 * When using the `HttpClient` object as the client, it actually creates an
 * instance of `HttpClient` for one-time usage. If you plan to send multiple
 * requests, you can create and maintain a reference to an instance, and use it
 * as the client. With that, you also get access to methods corresponding to the
 * standard HTTP request methods.
 *
 * {{{
 * import scamper.BodyParsers
 * import scamper.ImplicitConverters.stringToUri
 * import scamper.client.HttpClient
 *
 * implicit val parser = BodyParsers.text()
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

  /** Provides utility for sending request and handling response. */
  trait HttpClient {
    /**
     * Gets buffer size.
     *
     * The buffer size is used for setting each socket's buffer size.
     */
    def bufferSize: Int

    /**
     * Gets read timeout.
     *
     * The read timeout controls how long a read from a socket blocks before it
     * times out, whereafter the client throws `SocketTimeoutException`.
     */
    def readTimeout: Int

    /**
     * Sends request and passes response to supplied handler.
     *
     * To make effective use of this method, either the request's Host header
     * must be set, or the request target must be absolute. Note if the target
     * is absolute, its scheme is overridden in accordance to `secure`.
     *
     * @param request outgoing request
     * @param secure specifies whether to use HTTPS
     * @param handler response handler
     *
     * @return value from response handler
     */
    def send[T](request: HttpRequest, secure: Boolean = false)(handler: ResponseHandler[T]): T

    /**
     * Sends GET request and passes response to handler.
     *
     * <strong>Note:</strong> `target` must be absolute with a scheme of either
     * `http` or `https`.
     *
     * @param target request target
     * @param header request headers
     * @param cookies request cookies
     * @param handler response handler
     *
     * @return value from response handler
     */
    def get[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

    /**
     * Sends POST request and passes response to handler.
     *
     * <strong>Note:</strong> `target` must be absolute with a scheme of either
     * `http` or `https`.
     *
     * @param target request target
     * @param header request headers
     * @param cookies request cookies
     * @param body message body
     * @param handler response handler
     *
     * @return value from response handler
     */
    def post[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty())
      (handler: ResponseHandler[T]): T

    /**
     * Sends PUT request and passes response to handler.
     *
     * <strong>Note:</strong> `target` must be absolute with a scheme of either
     * `http` or `https`.
     *
     * @param target request target
     * @param header request headers
     * @param cookies request cookies
     * @param body message body
     * @param handler response handler
     *
     * @return value from response handler
     */
    def put[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty())
      (handler: ResponseHandler[T]): T

    /**
     * Sends PATCH request and passes response to handler.
     *
     * <strong>Note:</strong> `target` must be absolute with a scheme of either
     * `http` or `https`.
     *
     * @param target request target
     * @param header request headers
     * @param cookies request cookies
     * @param body message body
     * @param handler response handler
     *
     * @return value from response handler
     */
    def patch[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty())
      (handler: ResponseHandler[T]): T

    /**
     * Sends DELETE request and passes response to handler.
     *
     * <strong>Note:</strong> `target` must be absolute with a scheme of either
     * `http` or `https`.
     *
     * @param target request target
     * @param header request headers
     * @param cookies request cookies
     * @param handler response handler
     *
     * @return value from response handler
     */
    def delete[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

    /**
     * Sends HEAD request and passes response to handler.
     *
     * <strong>Note:</strong> `target` must be absolute with a scheme of either
     * `http` or `https`.
     *
     * @param target request target
     * @param header request headers
     * @param cookies request cookies
     * @param handler response handler
     *
     * @return value from response handler
     */
    def head[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil)(handler: ResponseHandler[T]): T

    /**
     * Sends OPTIONS request and passes response to handler.
     *
     * <strong>Note:</strong> `target` must be absolute with a scheme of either
     * `http` or `https`.
     *
     * @param target request target
     * @param header request headers
     * @param cookies request cookies
     * @param body message body
     * @param handler response handler
     *
     * @return value from response handler
     */
    def options[T](target: URI, headers: Seq[Header] = Nil, cookies: Seq[PlainCookie] = Nil, body: Entity = Entity.empty())
      (handler: ResponseHandler[T]): T

    /**
     * Sends TRACE request and passes response to handler.
     *
     * <strong>Note:</strong> `target` must be absolute with a scheme of either
     * `http` or `https`.
     *
     * @param target request target
     * @param header request headers
     * @param handler response handler
     *
     * @return value from response handler
     */
    def trace[T](target: URI, headers: Seq[Header] = Nil)(handler: ResponseHandler[T]): T
  }

  /** Provides factory for `HttpClient`. */
  object HttpClient {
    /**
     * Creates `HttpClient` with supplied configuration.
     *
     * @param bufferSize socket buffer size
     * @param readTimeout socket read timeout
     * @param trustStore trust store used for SSL/TLS <em>(<strong>Note:</strong>
     *   Type must be JKS.)</em>
     */
    def apply(bufferSize: Int = 8192, readTimeout: Int = 5000, trustStore: Option[File] = None): HttpClient =
      trustStore match {
        case Some(file) => DefaultHttpClient(bufferSize, readTimeout, file)
        case None       => DefaultHttpClient(bufferSize, readTimeout)
      }

    /**
     * Sends request and passes response to supplied handler.
     *
     * To make effective use of this method, either the request's Host header
     * must be set, or the request target must be absolute. Note if the target
     * is absolute, its scheme is overridden in accordance to `secure`.
     *
     * @param request HTTP request
     * @param secure specifies whether to use HTTPS
     * @param bufferSize socket buffer size
     * @param readTimeout socket read timeout
     * @param trustStore trust store used for SSL/TLS <em>(<strong>Note:</strong>
     *   Type must be JKS.)</em>
     * @param handler response handler
     *
     * @return value from applied handler
     */
    def send[T](request: HttpRequest, secure: Boolean = false, bufferSize: Int = 8192, readTimeout: Int = 5000, trustStore: Option[File] = None)
      (handler: ResponseHandler[T]): T = HttpClient(bufferSize, readTimeout, trustStore).send(request, secure)(handler)
  }
}
