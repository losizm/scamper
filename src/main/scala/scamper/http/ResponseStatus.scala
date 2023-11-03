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
package http

import java.io.{ File, InputStream, Reader }

import scala.collection.mutable.TreeMap

/**
 * Defines HTTP response status.
 *
 * @see [[ResponseStatus.Registry]]
 */
sealed trait ResponseStatus:
  /** Gets status code. */
  def statusCode: Int

  /** Gets reason phrase. */
  def reasonPhrase: String

  /** Tests for informational status code. */
  def isInformational: Boolean =
    statusCode >= 100 && statusCode <= 199

  /** Tests for successful status code. */
  def isSuccessful: Boolean =
    statusCode >= 200 && statusCode <= 299

  /** Tests for redirection status code. */
  def isRedirection: Boolean =
    statusCode >= 300 && statusCode <= 399

  /** Tests for client error status code. */
  def isClientError: Boolean =
    statusCode >= 400 && statusCode <= 499

  /** Tests for server error status code. */
  def isServerError: Boolean =
    statusCode >= 500 && statusCode <= 599

  /** Creates `HttpResponse` with this status and supplied body. */
  def apply(body: Entity = Entity.empty): HttpResponse =
    HttpResponse(this, body = body)

  /** Creates `HttpResponse` with this status and supplied body. */
  def apply(body: InputStream): HttpResponse =
    HttpResponse(this, body = Entity(body))

  /** Creates `HttpResponse` with this status and supplied body. */
  def apply(body: Reader): HttpResponse =
    HttpResponse(this, body = Entity(body))

  /** Creates `HttpResponse` with this status and supplied body. */
  def apply(body: Array[Byte]): HttpResponse =
    HttpResponse(this, body = Entity(body))

  /** Creates `HttpResponse` with this status and supplied body. */
  def apply(body: String): HttpResponse =
    HttpResponse(this, body = Entity(body))

  /** Creates `HttpResponse` with this status and supplied body. */
  def apply(body: File): HttpResponse =
    HttpResponse(this, body = Entity(body))

/**
 * Provides factory for `ResponseStatus`.
 *
 * @see [[ResponseStatus.Registry]]
 */
object ResponseStatus:
  /** Contains registered response statuses. */
  object Registry:
    private val registry = TreeMap[Int, ResponseStatus]()

    /** 100 Continue */
    val Continue = add(100, "Continue")

    /** 101 Switching Protocols */
    val SwitchingProtocols = add(101, "Switching Protocols")

    /** 103 Early Hints */
    val EarlyHints = add(103, "Early Hints")

    /** 200 OK */
    val Ok = add(200, "OK")

    /** 201 Created */
    val Created = add(201, "Created")

    /** 202 Accepted */
    val Accepted = add(202, "Accepted")

    /** 203 Non-Authoritative Information */
    val NonAuthoritativeInformation = add(203, "Non-Authoritative Information")

    /** 204 No Content */
    val NoContent = add(204, "No Content")

    /** 205 Reset Content */
    val ResetContent = add(205, "Reset Content")

    /** 206 Partial Content */
    val PartialContent = add(206, "Partial Content")

    /** 300 Multiple Choices */
    val MultipleChoices = add(300, "Multiple Choices")

    /** 301 Moved Permanently */
    val MovedPermanently = add(301, "Moved Permanently")

    /** 302 Found */
    val Found = add(302, "Found")

    /** 303 See Other */
    val SeeOther = add(303, "See Other")

    /** 304 Not Modified */
    val NotModified = add(304, "Not Modified")

    /** 305 Use Proxy */
    val UseProxy = add(305, "Use Proxy")

    /** 307 Temporary Redirect */
    val TemporaryRedirect = add(307, "Temporary Redirect")

    /** 308 Permanent Redirect */
    val PermanentRedirect = add(308, "Permanent Redirect")

    /** 400 Bad Request */
    val BadRequest = add(400, "Bad Request")

    /** 401 Unauthorized */
    val Unauthorized = add(401, "Unauthorized")

    /** 402 Payment Required */
    val PaymentRequired = add(402, "Payment Required")

    /** 403 Forbidden */
    val Forbidden = add(403, "Forbidden")

    /** 404 Not Found */
    val NotFound = add(404, "Not Found")

    /** 405 Method Not Allowed */
    val MethodNotAllowed = add(405, "Method Not Allowed")

    /** 406 Not Acceptable */
    val NotAcceptable = add(406, "Not Acceptable")

    /** 407 Proxy Authentication Required */
    val ProxyAuthenticationRequired = add(407, "Proxy Authentication Required")

    /** 408 Request Timeout */
    val RequestTimeout = add(408, "Request Timeout")

    /** 409 Conflict */
    val Conflict = add(409, "Conflict")

    /** 410 Gone */
    val Gone = add(410, "Gone")

    /** 411 Length Required */
    val LengthRequired = add(411, "Length Required")

    /** 412 Precondition Failed */
    val PreconditionFailed = add(412, "Precondition Failed")

    /** 413 Payload Too Large */
    val PayloadTooLarge = add(413, "Payload Too Large")

    /** 414 URI Too Long */
    val UriTooLong = add(414, "URI Too Long")

    /** 415 Unsupported Media Type */
    val UnsupportedMediaType = add(415, "Unsupported Media Type")

    /** 416 Range Not Satisfiable */
    val RangeNotSatisfiable = add(416, "Range Not Satisfiable")

    /** 417 Expectation Failed */
    val ExpectationFailed = add(417, "Expectation Failed")

    /** 422 Unprocessable Entity */
    val UnprocessableEntity = add(422, "Unprocessable Entity")

    /** 425 Too Early */
    val TooEarly = add(425, "Too Early")

    /** 426 Upgrade Required */
    val UpgradeRequired = add(426, "Upgrade Required")

    /** 428 Precondition Required */
    val PreconditionRequired = add(428, "Precondition Required")

    /** 429 Too Many Requests */
    val TooManyRequests = add(429, "Too Many Requests")

    /** 431 Request Header Fields Too Large */
    val RequestHeaderFieldsTooLarge = add(431, "Request Header Fields Too Large")

    /** 451 Unavailable For Legal Reasons */
    val UnavailableForLegalReasons = add(451, "Unavailable For Legal Reasons")

    /** 500 Internal Server Error */
    val InternalServerError = add(500, "Internal Server Error")

    /** 501 Not Implemented */
    val NotImplemented = add(501, "Not Implemented")

    /** 502 Bad Gateway */
    val BadGateway = add(502, "Bad Gateway")

    /** 503 Service Unavailable */
    val ServiceUnavailable = add(503, "Service Unavailable")

    /** 504 Gateway Timeout */
    val GatewayTimeout = add(504, "Gateway Timeout")

    /** 505 HTTP Version Not Supported */
    val HttpVersionNotSupported = add(505, "HTTP Version Not Supported")

    /** 511 Network Authentication Required */
    val NetworkAuthenticationRequired = add(511, "Network Authentication Required")

    private def add(statusCode: Int, reasonPhrase: String): ResponseStatus =
      val status = ResponseStatusImpl(statusCode, reasonPhrase)
      registry += { statusCode -> status }
      status

    private[ResponseStatus] def apply(statusCode: Int): ResponseStatus = registry(statusCode)
    private[ResponseStatus] def get(statusCode: Int): Option[ResponseStatus] = registry.get(statusCode)

  /** Gets response status for given status code, if registered. */
  def get(statusCode: Int): Option[ResponseStatus] = Registry.get(statusCode)

  /**
   * Gets registered response status for given status code.
   *
   * Throws `NoSuchElementException` if status code is not registered.
   */
  def apply(statusCode: Int): ResponseStatus = Registry(statusCode)

  /** Creates response status with supplied status code and reason phrase. */
  def apply(statusCode: Int, reasonPhrase: String): ResponseStatus =
    Registry.get(statusCode).filter(_.reasonPhrase == reasonPhrase).getOrElse {
      require(statusCode >= 100 && statusCode <= 599, s"status code out of bounds: $statusCode")
      ResponseStatusImpl(statusCode, reasonPhrase)
    }

private case class ResponseStatusImpl(statusCode: Int, reasonPhrase: String) extends ResponseStatus:
  override lazy val toString = s"$statusCode $reasonPhrase"
