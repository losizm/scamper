package scamper

/** Provides the status code and reason phrase of an HTTP response. */
case class Status(code: Int, reason: String)

/** Status factory */
object Status {
  /** 100 Continue */
  val Continue                = Status(100, "Continue")
  /** 200 OK */
  val OK                      = Status(200, "OK")
  /** 201 Created */
  val Created                 = Status(201, "Created")
  /** 202 Accepted */
  val Accepted                = Status(202, "Accepted")
  /** 204 No Content */
  val NoContent               = Status(204, "No Content")
  /** 205 Reset Content */
  val ResetContent            = Status(205, "Reset Content")
  /** 301 Moved Permanently */
  val MovedPermanently        = Status(301, "Moved Permanently")
  /** 302 Found */
  val Found                   = Status(302, "Found")
  /** 303 See Other */
  val SeeOther                = Status(303, "See Other")
  /** 304 Not Modified */
  val NotModified             = Status(304, "Not Modified")
  /** 307 Temporary Redirect */
  val TemporaryRedirect       = Status(307, "Temporary Redirect")
  /** 400 Bad Request */
  val BadRequest              = Status(400, "Bad Request")
  /** 401 Unauthorized */
  val Unauthorized            = Status(401, "Unauthorized")
  /** 403 Forbiddend */
  val Forbidden               = Status(403, "Forbidden")
  /** 404 Not Found */
  val NotFound                = Status(404, "Not Found")
  /** 405 Method Not Allowed */
  val MethodNotAllowed        = Status(405, "Method Not Allowed")
  /** 406 Not Acceptabled */
  val NotAcceptable           = Status(406, "Not Acceptable")
  /** 408 Request Timeout */
  val RequestTimeout          = Status(408, "Request Timeout")
  /** 409 Conflict */
  val Conflict                = Status(409, "Conflict")
  /** 410 Gone */
  val Gone                    = Status(410, "Gone")
  /** 411 Length Required */
  val LengthRequired          = Status(411, "Length Required")
  /** 413 Request Entity Too Long */
  val RequestEntityTooLong    = Status(413, "Request Entity Too Long")
  /** 413 Request URI Too Long */
  val RequestUriTooLong       = Status(414, "Request URI Too Long")
  /** 417 Expectation Failed */
  val ExpectationFailed       = Status(417, "Expectation Failed")
  /** 500 Internal Server Error */
  val InternalServerError     = Status(500, "Internal Server Error")
  /** 501 Not Implemented */
  val NotImplemented          = Status(501, "Not Implemented")
  /** 503 Service Unavailable */
  val ServiceUnavailable      = Status(503, "Service Unavailable")
  /** 505 HTTP Version Not Supported */
  val HttpVersionNotSupported = Status(505, "HTTP Version Not Supported")
}

