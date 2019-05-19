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

/** Includes standard HTTP statuses. */
object ResponseStatuses {
  /** 100 Continue */
  val Continue = ResponseStatus(100)

  /** 101 Switching Protocols */
  val SwitchingProtocols = ResponseStatus(101)

  /** 103 Early Hints */
  val EarlyHints = ResponseStatus(103)

  /** 200 OK */
  val Ok = ResponseStatus(200)

  /** 201 Created */
  val Created = ResponseStatus(201)

  /** 202 Accepted */
  val Accepted = ResponseStatus(202)

  /** 203 Non-Authoritative Information */
  val NonAuthoritativeInformation = ResponseStatus(203)

  /** 204 No Content */
  val NoContent = ResponseStatus(204)

  /** 205 Reset Content */
  val ResetContent = ResponseStatus(205)

  /** 206 Partial Content */
  val PartialContent = ResponseStatus(206)

  /** 300 Multiple Choices */
  val MultipleChoices = ResponseStatus(300)

  /** 301 Moved Permanently */
  val MovedPermanently = ResponseStatus(301)

  /** 302 Found */
  val Found = ResponseStatus(302)

  /** 303 See Other */
  val SeeOther = ResponseStatus(303)

  /** 304 Not Modified */
  val NotModified = ResponseStatus(304)

  /** 305 Use Proxy */
  val UseProxy = ResponseStatus(305)

  /** 307 Temporary Redirect */
  val TemporaryRedirect = ResponseStatus(307)

  /** 308 Permanent Redirect */
  val PermanentRedirect = ResponseStatus(308)

  /** 400 Bad Request */
  val BadRequest = ResponseStatus(400)

  /** 401 Unauthorized */
  val Unauthorized = ResponseStatus(401)

  /** 402 Payment Required */
  val PaymentRequired = ResponseStatus(402)

  /** 403 Forbidden */
  val Forbidden = ResponseStatus(403)

  /** 404 Not Found */
  val NotFound = ResponseStatus(404)

  /** 405 Method Not Allowed */
  val MethodNotAllowed = ResponseStatus(405)

  /** 406 Not Acceptable */
  val NotAcceptable = ResponseStatus(406)

  /** 407 Proxy Authentication Required */
  val ProxyAuthenticationRequired = ResponseStatus(407)

  /** 408 Request Timeout */
  val RequestTimeout = ResponseStatus(408)

  /** 409 Conflict */
  val Conflict = ResponseStatus(409)

  /** 410 Gone */
  val Gone = ResponseStatus(410)

  /** 411 Length Required */
  val LengthRequired = ResponseStatus(411)

  /** 412 Precondition Failed */
  val PreconditionFailed = ResponseStatus(412)

  /** 413 Payload Too Large */
  val PayloadTooLarge = ResponseStatus(413)

  /** 414 URI Too Long */
  val UriTooLong = ResponseStatus(414)

  /** 415 Unsupported Media Type */
  val UnsupportedMediaType = ResponseStatus(415)

  /** 416 Range Not Satisfiable */
  val RangeNotSatisfiable = ResponseStatus(416)

  /** 417 Expectation Failed */
  val ExpectationFailed = ResponseStatus(417)

  /** 422 Unprocessable Entity */
  val UnprocessableEntity = ResponseStatus(422)

  /** 425 Too Early */
  val TooEarly = ResponseStatus(425)

  /** 426 Upgrade Required */
  val UpgradeRequired = ResponseStatus(426)

  /** 428 Precondition Required */
  val PreconditionRequired = ResponseStatus(428)

  /** 429 Too Many Requests */
  val TooManyRequests = ResponseStatus(429)

  /** 431 Request Header Fields Too Large */
  val RequestHeaderFieldsTooLarge = ResponseStatus(431)

  /** 451 Unavailable For Legal Reasons */
  val UnavailableForLegalReasons = ResponseStatus(451)

  /** 500 Internal Server Error */
  val InternalServerError = ResponseStatus(500)

  /** 501 Not Implemented */
  val NotImplemented = ResponseStatus(501)

  /** 502 Bad Gateway */
  val BadGateway = ResponseStatus(502)

  /** 503 Service Unavailable */
  val ServiceUnavailable = ResponseStatus(503)

  /** 504 Gateway Timeout */
  val GatewayTimeout = ResponseStatus(504)

  /** 505 HTTP Version Not Supported */
  val HttpVersionNotSupported = ResponseStatus(505)

  /** 511 Network Authentication Required */
  val NetworkAuthenticationRequired = ResponseStatus(511)
}
