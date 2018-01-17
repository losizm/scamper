package scamper

/** Provides the status code and reason phrase of an HTTP response. */
case class Status(code: Int, reason: String)

/** Status factory */
object Status {
  /** 100 Continue */
  val Continue = Status(100, "Continue")

  /** 101 Switching Protocols */
  val SwitchingProtocols = Status(101, "Switching Protocols")

  /** 200 OK */
  val OK = Status(200, "OK")

  /** 201 Created */
  val Created = Status(201, "Created")

  /** 202 Accepted */
  val Accepted = Status(202, "Accepted")

  /** 203 Non-Authoritative Information */
  val NonAuthoritativeInformation = Status(203, "Non-Authoritative Information")

  /** 204 No Content */
  val NoContent = Status(204, "No Content")

  /** 205 Reset Content */
  val ResetContent = Status(205, "Reset Content")

  /** 206 Partial Content */
  val PartialContent = Status(206, "Partial Content")

  /** 300 Multiple Choices */
  val MultipleChoices = Status(300, "Multiple Choices")

  /** 301 Moved Permanently */
  val MovedPermanently = Status(301, "Moved Permanently")

  /** 302 Found */
  val Found = Status(302, "Found")

  /** 303 See Other */
  val SeeOther = Status(303, "See Other")

  /** 304 Not Modified */
  val NotModified = Status(304, "Not Modified")

  /** 305 Use Proxy */
  val UseProxy = Status(305, "Use Proxy")

  /** 307 Temporary Redirect */
  val TemporaryRedirect = Status(307, "Temporary Redirect")

  /** 400 Bad Request */
  val BadRequest = Status(400, "Bad Request")

  /** 401 Unauthorized */
  val Unauthorized = Status(401, "Unauthorized")

  /** 402 Payment Required */
  val PaymentRequired = Status(402, "Payment Required")

  /** 403 Forbidden */
  val Forbidden = Status(403, "Forbidden")

  /** 404 Not Found */
  val NotFound = Status(404, "Not Found")

  /** 405 Method Not Allowed */
  val MethodNotAllowed = Status(405, "Method Not Allowed")

  /** 406 Not Acceptabled */
  val NotAcceptable = Status(406, "Not Acceptable")

  /** 407 Proxy Authentication Required */
  val ProxyAuthenticationRequired = Status(407, "Proxy Authentication Required")

  /** 408 Request Timeout */
  val RequestTimeout = Status(408, "Request Timeout")

  /** 409 Conflict */
  val Conflict = Status(409, "Conflict")

  /** 410 Gone */
  val Gone = Status(410, "Gone")

  /** 411 Length Required */
  val LengthRequired = Status(411, "Length Required")

  /** 412 Precondition Failed */
  val PreconditionFailed = Status(412, "Precondition Failed")

  /** 413 Request Entity Too Long */
  val RequestEntityTooLong = Status(413, "Request Entity Too Long")

  /** 414 Request URI Too Long */
  val RequestUriTooLong = Status(414, "Request URI Too Long")

  /** 415 Unsupported Media Type */
  val UnsupportedMediaType = Status(415, "Unsupported Media Type")

  /** 416 Requested Range Not Satisfiable */
  val RequestedRangeNotSatisfiable = Status(416, "Requested Range Not Satisfiable")

  /** 417 Expectation Failed */
  val ExpectationFailed = Status(417, "Expectation Failed")

  /** 500 Internal Server Error */
  val InternalServerError = Status(500, "Internal Server Error")

  /** 501 Not Implemented */
  val NotImplemented = Status(501, "Not Implemented")

  /** 502 Bad Gateway */
  val BadGateway = Status(502, "Bad Gateway")

  /** 503 Service Unavailable */
  val ServiceUnavailable = Status(503, "Service Unavailable")

  /** 504 Gateway Timeout */
  val GatewayTimeout = Status(504, "Gateway Timeout")

  /** 505 HTTP Version Not Supported */
  val HttpVersionNotSupported = Status(505, "HTTP Version Not Supported")
}

