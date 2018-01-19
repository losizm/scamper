package scamper

/** Provides the status code and reason phrase of an HTTP response. */
case class Status(code: Int, reason: String) {
  /** Tests whether this is an informational status. */
  def isInformational: Boolean =
    code >= 100 && code <= 199

  /** Tests whether this is a successful status. */
  def isSuccessful: Boolean =
    code >= 200 && code <= 299

  /** Tests whether this is a redirection status. */
  def isRedirection: Boolean =
    code >= 300 && code <= 399

  /** Tests whether this is a client error status. */
  def isClientError: Boolean =
    code >= 400 && code <= 499

  /** Tests whether this is a server error status. */
  def isServerError: Boolean =
    code >= 500 && code <= 599
}

/** Status factory */
object Status {
  private val statuses = new scala.collection.mutable.TreeMap[Int, Status]

  /** 100 Continue */
  val Continue = newStatus(100, "Continue")

  /** 101 Switching Protocols */
  val SwitchingProtocols = newStatus(101, "Switching Protocols")

  /** 200 OK */
  val Ok = newStatus(200, "OK")

  /** 201 Created */
  val Created = newStatus(201, "Created")

  /** 202 Accepted */
  val Accepted = newStatus(202, "Accepted")

  /** 203 Non-Authoritative Information */
  val NonAuthoritativeInformation = newStatus(203, "Non-Authoritative Information")

  /** 204 No Content */
  val NoContent = newStatus(204, "No Content")

  /** 205 Reset Content */
  val ResetContent = newStatus(205, "Reset Content")

  /** 206 Partial Content */
  val PartialContent = newStatus(206, "Partial Content")

  /** 300 Multiple Choices */
  val MultipleChoices = newStatus(300, "Multiple Choices")

  /** 301 Moved Permanently */
  val MovedPermanently = newStatus(301, "Moved Permanently")

  /** 302 Found */
  val Found = newStatus(302, "Found")

  /** 303 See Other */
  val SeeOther = newStatus(303, "See Other")

  /** 304 Not Modified */
  val NotModified = newStatus(304, "Not Modified")

  /** 305 Use Proxy */
  val UseProxy = newStatus(305, "Use Proxy")

  /** 307 Temporary Redirect */
  val TemporaryRedirect = newStatus(307, "Temporary Redirect")

  /** 400 Bad Request */
  val BadRequest = newStatus(400, "Bad Request")

  /** 401 Unauthorized */
  val Unauthorized = newStatus(401, "Unauthorized")

  /** 402 Payment Required */
  val PaymentRequired = newStatus(402, "Payment Required")

  /** 403 Forbidden */
  val Forbidden = newStatus(403, "Forbidden")

  /** 404 Not Found */
  val NotFound = newStatus(404, "Not Found")

  /** 405 Method Not Allowed */
  val MethodNotAllowed = newStatus(405, "Method Not Allowed")

  /** 406 Not Acceptabled */
  val NotAcceptable = newStatus(406, "Not Acceptable")

  /** 407 Proxy Authentication Required */
  val ProxyAuthenticationRequired = newStatus(407, "Proxy Authentication Required")

  /** 408 Request Timeout */
  val RequestTimeout = newStatus(408, "Request Timeout")

  /** 409 Conflict */
  val Conflict = newStatus(409, "Conflict")

  /** 410 Gone */
  val Gone = newStatus(410, "Gone")

  /** 411 Length Required */
  val LengthRequired = newStatus(411, "Length Required")

  /** 412 Precondition Failed */
  val PreconditionFailed = newStatus(412, "Precondition Failed")

  /** 413 Request Entity Too Long */
  val RequestEntityTooLong = newStatus(413, "Request Entity Too Long")

  /** 414 Request URI Too Long */
  val RequestUriTooLong = newStatus(414, "Request URI Too Long")

  /** 415 Unsupported Media Type */
  val UnsupportedMediaType = newStatus(415, "Unsupported Media Type")

  /** 416 Requested Range Not Satisfiable */
  val RequestedRangeNotSatisfiable = newStatus(416, "Requested Range Not Satisfiable")

  /** 417 Expectation Failed */
  val ExpectationFailed = newStatus(417, "Expectation Failed")

  /** 500 Internal Server Error */
  val InternalServerError = newStatus(500, "Internal Server Error")

  /** 501 Not Implemented */
  val NotImplemented = newStatus(501, "Not Implemented")

  /** 502 Bad Gateway */
  val BadGateway = newStatus(502, "Bad Gateway")

  /** 503 Service Unavailable */
  val ServiceUnavailable = newStatus(503, "Service Unavailable")

  /** 504 Gateway Timeout */
  val GatewayTimeout = newStatus(504, "Gateway Timeout")

  /** 505 HTTP Version Not Supported */
  val HttpVersionNotSupported = newStatus(505, "HTTP Version Not Supported")

  /**
   * Gets defined status for given code. <code>NoSuchElementException</code> is
   * thrown if a status is not defined for code.
   */
  def apply(code: Int): Status =
    statuses(code)

  /** Gets defined status for given code. */
  def get(code: Int): Option[Status] =
    statuses.get(code)

  private def newStatus(code: Int, reason: String) =
    statuses.getOrElseUpdate(code, Status(code, reason))
}

