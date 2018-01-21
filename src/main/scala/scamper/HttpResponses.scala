package scamper

/** Provides the standard responses. */
object HttpResponses {
  /** 100 Continue */
  val Continue = HttpResponse(Status(100, "Continue"))

  /** 101 Switching Protocols */
  val SwitchingProtocols = HttpResponse(Status(101, "Switching Protocols"))

  /** 200 OK */
  val Ok = HttpResponse(Status(200, "OK"))

  /** 201 Created */
  val Created = HttpResponse(Status(201, "Created"))

  /** 202 Accepted */
  val Accepted = HttpResponse(Status(202, "Accepted"))

  /** 203 Non-Authoritative Information */
  val NonAuthoritativeInformation = HttpResponse(Status(203, "Non-Authoritative Information"))

  /** 204 No Content */
  val NoContent = HttpResponse(Status(204, "No Content"))

  /** 205 Reset Content */
  val ResetContent = HttpResponse(Status(205, "Reset Content"))

  /** 206 Partial Content */
  val PartialContent = HttpResponse(Status(206, "Partial Content"))

  /** 300 Multiple Choices */
  val MultipleChoices = HttpResponse(Status(300, "Multiple Choices"))

  /** 301 Moved Permanently */
  val MovedPermanently = HttpResponse(Status(301, "Moved Permanently"))

  /** 302 Found */
  val Found = HttpResponse(Status(302, "Found"))

  /** 303 See Other */
  val SeeOther = HttpResponse(Status(303, "See Other"))

  /** 304 Not Modified */
  val NotModified = HttpResponse(Status(304, "Not Modified"))

  /** 305 Use Proxy */
  val UseProxy = HttpResponse(Status(305, "Use Proxy"))

  /** 307 Temporary Redirect */
  val TemporaryRedirect = HttpResponse(Status(307, "Temporary Redirect"))

  /** 400 Bad Request */
  val BadRequest = HttpResponse(Status(400, "Bad Request"))

  /** 401 Unauthorized */
  val Unauthorized = HttpResponse(Status(401, "Unauthorized"))

  /** 402 Payment Required */
  val PaymentRequired = HttpResponse(Status(402, "Payment Required"))

  /** 403 Forbidden */
  val Forbidden = HttpResponse(Status(403, "Forbidden"))

  /** 404 Not Found */
  val NotFound = HttpResponse(Status(404, "Not Found"))

  /** 405 Method Not Allowed */
  val MethodNotAllowed = HttpResponse(Status(405, "Method Not Allowed"))

  /** 406 Not Acceptabled */
  val NotAcceptable = HttpResponse(Status(406, "Not Acceptable"))

  /** 407 Proxy Authentication Required */
  val ProxyAuthenticationRequired = HttpResponse(Status(407, "Proxy Authentication Required"))

  /** 408 Request Timeout */
  val RequestTimeout = HttpResponse(Status(408, "Request Timeout"))

  /** 409 Conflict */
  val Conflict = HttpResponse(Status(409, "Conflict"))

  /** 410 Gone */
  val Gone = HttpResponse(Status(410, "Gone"))

  /** 411 Length Required */
  val LengthRequired = HttpResponse(Status(411, "Length Required"))

  /** 412 Precondition Failed */
  val PreconditionFailed = HttpResponse(Status(412, "Precondition Failed"))

  /** 413 Request Entity Too Long */
  val RequestEntityTooLong = HttpResponse(Status(413, "Request Entity Too Long"))

  /** 414 Request URI Too Long */
  val RequestUriTooLong = HttpResponse(Status(414, "Request URI Too Long"))

  /** 415 Unsupported Media Type */
  val UnsupportedMediaType = HttpResponse(Status(415, "Unsupported Media Type"))

  /** 416 Requested Range Not Satisfiable */
  val RequestedRangeNotSatisfiable = HttpResponse(Status(416, "Requested Range Not Satisfiable"))

  /** 417 Expectation Failed */
  val ExpectationFailed = HttpResponse(Status(417, "Expectation Failed"))

  /** 500 Internal Server Error */
  val InternalServerError = HttpResponse(Status(500, "Internal Server Error"))

  /** 501 Not Implemented */
  val NotImplemented = HttpResponse(Status(501, "Not Implemented"))

  /** 502 Bad Gateway */
  val BadGateway = HttpResponse(Status(502, "Bad Gateway"))

  /** 503 Service Unavailable */
  val ServiceUnavailable = HttpResponse(Status(503, "Service Unavailable"))

  /** 504 Gateway Timeout */
  val GatewayTimeout = HttpResponse(Status(504, "Gateway Timeout"))

  /** 505 HTTP Version Not Supported */
  val HttpVersionNotSupported = HttpResponse(Status(505, "HTTP Version Not Supported"))
}

