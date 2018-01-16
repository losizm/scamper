package scamper

import scala.util.Try

/**
 * A representation of an HTTP message.
 *
 * @see [[HttpRequest]], [[HttpResponse]]
 */
trait HttpMessage {
  type MessageType <: HttpMessage

  /** The message start line */
  def startLine: StartLine

  /** The sequence of message headers */
  def headers: Seq[Header]

  /**
   * Gets header value for specified key.
   *
   * If there are multiple headers for key, then the value of first header
   * occurrence is retreived.
   */
  def getHeaderValue(key: String): Option[String] =
    headers.collectFirst {
      case Header(k, value) if k.equalsIgnoreCase(key) => value
    }

  /** Gets all header values for specified key. */
  def getHeaderValues(key: String): List[String] =
    headers.collect {
      case Header(k, value) if k.equalsIgnoreCase(key) => value
    }.toList

  /** The message body */
  def body: Option[Entity]

  /** Parses the message body. */
  def parse[T](implicit bodyParser: BodyParser[T]): Try[T] =
    Try(bodyParser(this))

  /**
   * Gets the content type.
   *
   * The value is retrieved from the Content-Type header.
   */
  def contentType: Option[ContentType] =
    getHeaderValue("Content-Type").map(ContentType.apply)

  /**
   * Gets the content length.
   *
   * The value is retrieved from the Content-Length header.
   */
  def contentLength: Option[Long] =
    getHeaderValue("Content-Length").map(_.toLong)

  /**
   * Gets the content encoding.
   *
   * The value is retrieved from the Content-Encoding header.
   */
  def contentEncoding: Option[String] =
    getHeaderValue("Content-Encoding")

  /**
   * Tests whether the message body is chunked.
   *
   * This is determined by inspecting the Transfer-Encoding header.
   */
  def isChunked: Boolean =
    getHeaderValue("Transfer-Encoding").exists("chunked".equalsIgnoreCase)

  /**
   * Creates a copy of this message with the additional headers.
   *
   * @return the new message
   */
  def addHeaders(headers: Header*): MessageType

  /**
   * Creates a copy of this message with a new set of headers.
   *
   * @return the new message
   */
  def withHeaders(headers: Header*): MessageType

  /**
   * Creates a copy of this message with a new body.
   *
   * @return the new message
   */
  def withBody(body: Option[Entity]): MessageType
}

/** A representation of an HTTP request. */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest

  /** The request method (i.e., GET, POST, etc.) */
  def method: String

  /** The request URI */
  def uri: String

  /** HTTP version of request message */
  def version: Version

  lazy val startLine: RequestLine =
    RequestLine(method, uri, version)

  /**
   * Gets the requested host.
   *
   * The value is retrieved from the Host header.
   */
  def host: Option[String] =
    getHeaderValue("Host")

  /**
   * Creates a copy of this request with a new method.
   *
   * @return the new request
   */
  def withMethod(method: String): MessageType

  /**
   * Creates a copy of this request with a new URI.
   *
   * @return the new request
   */
  def withURI(uri: String): MessageType

  /**
   * Creates a copy of this request with a new version.
   *
   * @return the new request
   */
  def withVersion(version: Version): MessageType
}

/** HttpRequest factory */
object HttpRequest {
  /** Creates an HttpRequest using the supplied attributes. */
  def apply(method: String, uri: String, headers: Seq[Header] = Nil, body: Option[Entity] = None, version: Version = Version(1, 1)): HttpRequest =
    SimpleHttpRequest(method, uri, headers, body, version)

  /** Creates an HttpRequest using the supplied attributes. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Option[Entity]): HttpRequest =
    SimpleHttpRequest(requestLine.method, requestLine.uri, headers, body, requestLine.version)
}

private case class SimpleHttpRequest(method: String, uri: String, headers: Seq[Header], body: Option[Entity], version: Version) extends HttpRequest {
  def addHeaders(moreHeaders: Header*): MessageType =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Header*): MessageType =
    copy(headers = newHeaders)

  def withBody(newBody: Option[Entity]): MessageType =
    copy(body = newBody)

  def withMethod(newMethod: String): MessageType =
    copy(method = newMethod)

  def withURI(newURI: String): MessageType =
    copy(uri = newURI)

  def withVersion(newVersion: Version): MessageType =
    copy(version = newVersion)
}

/** A representation of an HTTP response. */
trait HttpResponse extends HttpMessage {
  type MessageType = HttpResponse

  /** The response status */
  def status: Status

  /** HTTP version of response message */
  def version: Version

  lazy val startLine: StatusLine =
    StatusLine(version, status)

  /**
   * Creates a copy of this response with a new status.
   *
   * @return the new response
   */
  def withStatus(status: Status): MessageType

  /**
   * Creates a copy of this response with a new version.
   *
   * @return the new response
   */
  def withVersion(version: Version): MessageType
}

/** HttpResponse factory */
object HttpResponse {
  /** Creates an HttpResponse using the supplied attributes. */
  def apply(status: Status, headers: Seq[Header] = Nil, body: Option[Entity] = None, version: Version = Version(1, 1)): HttpResponse =
    SimpleHttpResponse(status, headers, body, version)

  /** Creates an HttpResponse using the supplied attributes. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Option[Entity]): HttpResponse =
    SimpleHttpResponse(statusLine.status, headers, body, statusLine.version)
}

private case class SimpleHttpResponse(status: Status, headers: Seq[Header], body: Option[Entity], version: Version) extends HttpResponse {
  def addHeaders(moreHeaders: Header*): MessageType =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Header*): MessageType =
    copy(headers = newHeaders)

  def withBody(newBody: Option[Entity]): MessageType =
    copy(body = newBody)

  def withStatus(newStatus: Status): MessageType =
    copy(status = newStatus)

  def withVersion(newVersion: Version): MessageType =
    copy(version = newVersion)
}

