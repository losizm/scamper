package scamper

/** Provides standard HTTP statuses. */
trait Statuses {
  /** 100 Continue */
  val Continue = Status(100)

  /** 101 Switching Protocols */
  val SwitchingProtocols = Status(101)

  /** 200 OK */
  val Ok = Status(200)

  /** 201 Created */
  val Created = Status(201)

  /** 202 Accepted */
  val Accepted = Status(202)

  /** 203 Non-Authoritative Information */
  val NonAuthoritativeInformation = Status(203)

  /** 204 No Content */
  val NoContent = Status(204)

  /** 205 Reset Content */
  val ResetContent = Status(205)

  /** 206 Partial Content */
  val PartialContent = Status(206)

  /** 300 Multiple Choices */
  val MultipleChoices = Status(300)

  /** 301 Moved Permanently */
  val MovedPermanently = Status(301)

  /** 302 Found */
  val Found = Status(302)

  /** 303 See Other */
  val SeeOther = Status(303)

  /** 304 Not Modified */
  val NotModified = Status(304)

  /** 305 Use Proxy */
  val UseProxy = Status(305)

  /** 307 Temporary Redirect */
  val TemporaryRedirect = Status(307)

  /** 400 Bad Request */
  val BadRequest = Status(400)

  /** 401 Unauthorized */
  val Unauthorized = Status(401)

  /** 402 Payment Required */
  val PaymentRequired = Status(402)

  /** 403 Forbidden */
  val Forbidden = Status(403)

  /** 404 Not Found */
  val NotFound = Status(404)

  /** 405 Method Not Allowed */
  val MethodNotAllowed = Status(405)

  /** 406 Not Acceptabled */
  val NotAcceptable = Status(406)

  /** 407 Proxy Authentication Required */
  val ProxyAuthenticationRequired = Status(407)

  /** 408 Request Timeout */
  val RequestTimeout = Status(408)

  /** 409 Conflict */
  val Conflict = Status(409)

  /** 410 Gone */
  val Gone = Status(410)

  /** 411 Length Required */
  val LengthRequired = Status(411)

  /** 412 Precondition Failed */
  val PreconditionFailed = Status(412)

  /** 413 Request Entity Too Long */
  val RequestEntityTooLong = Status(413)

  /** 414 Request URI Too Long */
  val RequestUriTooLong = Status(414)

  /** 415 Unsupported Media Type */
  val UnsupportedMediaType = Status(415)

  /** 416 Requested Range Not Satisfiable */
  val RequestedRangeNotSatisfiable = Status(416)

  /** 417 Expectation Failed */
  val ExpectationFailed = Status(417)

  /** 500 Internal Server Error */
  val InternalServerError = Status(500)

  /** 501 Not Implemented */
  val NotImplemented = Status(501)

  /** 502 Bad Gateway */
  val BadGateway = Status(502)

  /** 503 Service Unavailable */
  val ServiceUnavailable = Status(503)

  /** 504 Gateway Timeout */
  val GatewayTimeout = Status(504)

  /** 505 HTTP Version Not Supported */
  val HttpVersionNotSupported = Status(505)
}

