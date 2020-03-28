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

import scala.collection.mutable.TreeMap

/**
 * HTTP response status
 *
 * @see [[ResponseStatus.Registry]]
 */
trait ResponseStatus {
  /** Gets status code. */
  def code: Int

  /** Gets reason phrase. */
  def reason: String

  /** Tests for informational status code. */
  def isInformational: Boolean =
    code >= 100 && code <= 199

  /** Tests for successful status code. */
  def isSuccessful: Boolean =
    code >= 200 && code <= 299

  /** Tests for redirection status code. */
  def isRedirection: Boolean =
    code >= 300 && code <= 399

  /** Tests for client error status code. */
  def isClientError: Boolean =
    code >= 400 && code <= 499

  /** Tests for server error status code. */
  def isServerError: Boolean =
    code >= 500 && code <= 599

  /** Creates `HttpResponse` with supplied body. */
  def apply(body: Entity = Entity.empty): HttpResponse =
    HttpResponse(this, Nil, body)

  /** Returns formatted response status. */
  override def toString(): String = s"$code $reason"
}

/**
 * Provides factory methods for `ResponseStatus`.
 *
 * @see [[ResponseStatus.Registry]]
 */
object ResponseStatus {
  /** Contains registered HTTP status codes. */
  object Registry {
    private val statuses = new TreeMap[Int, ResponseStatus]

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

    private def add(code: Int, reason: String): ResponseStatus = {
      val status = ResponseStatusImpl(code, reason)
      statuses += { code -> status }
      status
    }

    private[ResponseStatus] def apply(code: Int): ResponseStatus = statuses(code)
    private[ResponseStatus] def get(code: Int): Option[ResponseStatus] = statuses.get(code)
  }

  /** Gets `ResponseStatus` for given status code, if registered. */
  def get(code: Int): Option[ResponseStatus] = Registry.get(code)

  /**
   * Gets registered `ResponseStatus` for given status code.
   *
   * Throws `NoSuchElementException` if status code is not registered.
   */
  def apply(code: Int): ResponseStatus = Registry(code)

  /** Creates `ResponseStatus` with supplied code and reason. */
  def apply(code: Int, reason: String): ResponseStatus = {
    require(code >= 100 && code <= 599, s"code out of bounds: $code")
    ResponseStatusImpl(code, reason)
  }

  /** Destructures `ResponseStatus`. */
  def unapply(status: ResponseStatus): Option[(Int, String)] =
    Some((status.code, status.reason))
}

private case class ResponseStatusImpl(code: Int, reason: String) extends ResponseStatus
