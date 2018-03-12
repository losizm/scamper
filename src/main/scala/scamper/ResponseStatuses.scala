package scamper

/** Provides standard HTTP statuses. */
object ResponseStatuses {
  /** 100 Continue */
  val Continue = ResponseStatus(100)

  /** 101 Switching Protocols */
  val SwitchingProtocols = ResponseStatus(101)

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

  /** 406 Not Acceptabled */
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

  /** 413 Request Entity Too Long */
  val RequestEntityTooLong = ResponseStatus(413)

  /** 414 Request URI Too Long */
  val RequestUriTooLong = ResponseStatus(414)

  /** 415 Unsupported Media Type */
  val UnsupportedMediaType = ResponseStatus(415)

  /** 416 Requested Range Not Satisfiable */
  val RequestedRangeNotSatisfiable = ResponseStatus(416)

  /** 417 Expectation Failed */
  val ExpectationFailed = ResponseStatus(417)

  /** 426 Upgrade Required */
  val UpgradeRequired = ResponseStatus(426)

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
}

