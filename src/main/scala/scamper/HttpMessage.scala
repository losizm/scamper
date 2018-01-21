package scamper

import scala.util.Try

/**
 * A representation of an HTTP message.
 *
 * @see [[HttpRequest]], [[HttpResponse]]
 */
trait HttpMessage {
  type MessageType <: HttpMessage
  type LineType <: StartLine

  /** The message start line */
  def startLine: LineType

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
  def body: Entity

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
   * Creates a copy of this message replacing the supplied header.
   *
   * All previous headers having the same key as supplied header are removed and
   * replaced with the single header instance.
   *
   * @return the new message
   */
  def withHeader(header: Header): MessageType =
    withHeaders {
      headers.filterNot(_.key.equalsIgnoreCase(header.key)) :+ header : _*
    }

  /**
   * Creates a copy of this message removing all headers having the supplied
   * key.
   *
   * @return the new message
   */
  def withoutHeader(key: String): MessageType =
    withHeaders {
      headers.filterNot(_.key.equalsIgnoreCase(key)) : _*
    }

  /**
   * Creates a copy of this message including additional headers.
   *
   * @return the new message
   */
  def addHeaders(headers: Header*): MessageType

  /**
   * Creates a copy of this message replacing the headers.
   *
   * All previous headers are removed, and the new message contains only the
   * supplied headers.
   *
   * @return the new message
   */
  def withHeaders(headers: Header*): MessageType

  /**
   * Creates a copy of this message replacing the body.
   *
   * @return the new message
   */
  def withBody(body: Entity): MessageType

  /**
   * Creates a copy of this message replacing the content type.
   *
   * @return the new message
   */
  def withContentType(contentType: ContentType): MessageType =
    withHeader(Header("Content-Type", contentType.toString))

  /**
   * Creates a copy of this message replacing the content length.
   *
   * @return the new message
   */
  def withContentLength(length: Long): MessageType =
    withHeader(Header("Content-Length", length.toString))

  /**
   * Creates a copy of this message replacing the content encoding.
   *
   * @return the new message
   */
  def withContentEncoding(encoding: String): MessageType =
    withHeader(Header("Content-Encoding", encoding))

  /**
   * Creates a copy of this message replacing the transfer encoding.
   *
   * @return the new message
   */
  def withChunked: MessageType =
    withHeader(Header("Transfer-Encoding", "chunked"))
}

/** A representation of an HTTP request. */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest
  type LineType = RequestLine

  /** The request method (i.e., GET, POST, etc.) */
  def method: String

  /** The request URI */
  def uri: String

  /** HTTP version of request message */
  def version: Version

  /** The path component of URI */
  def path: String

  /** The query component of URI */
  def query: Option[String]

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
   * Creates a copy of this request replacing the request method.
   *
   * @return the new request
   */
  def withMethod(method: String): MessageType

  /**
   * Creates a copy of this request replacing the request URI.
   *
   * @return the new request
   */
  def withURI(uri: String): MessageType

  /**
   * Creates a copy of this request replacing the HTTP version.
   *
   * @return the new request
   */
  def withVersion(version: Version): MessageType

  /**
   * Creates a copy of this message replacing the host.
   *
   * @return the new message
   */
  def withHost(host: String): MessageType =
    withHeader(Header("Host", host))
}

/** Provides HttpRequest factory methods. */
object HttpRequest {
  /** Creates an HttpRequest using the supplied attributes. */
  def apply(method: String, uri: String, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpRequest =
    SimpleHttpRequest(method, uri, headers, body, version)

  /** Creates an HttpRequest using the supplied attributes. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    SimpleHttpRequest(requestLine.method, requestLine.uri, headers, body, requestLine.version)
}

private case class SimpleHttpRequest(method: String, uri: String, headers: Seq[Header], body: Entity, version: Version) extends HttpRequest {
  private lazy val uriObject = new java.net.URI(uri)

  lazy val path = uriObject.getPath
  lazy val query = Option(uriObject.getQuery)

  def addHeaders(moreHeaders: Header*): MessageType =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Header*): MessageType =
    copy(headers = newHeaders)

  def withBody(newBody: Entity): MessageType =
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
  type LineType = StatusLine

  /** The response status */
  def status: Status

  /** HTTP version of response message */
  def version: Version

  lazy val startLine: StatusLine =
    StatusLine(version, status)

  /**
   * Gets the location.
   *
   * The value is retrieved from the Location header.
   */
  def location: Option[String] =
    getHeaderValue("Location")

  /**
   * Creates a copy of this response replacing the response status.
   *
   * @return the new response
   */
  def withStatus(status: Status): MessageType

  /**
   * Creates a copy of this response replacing the HTTP version.
   *
   * @return the new response
   */
  def withVersion(version: Version): MessageType

  /**
   * Creates a copy of this message replacing the location.
   *
   * @return the new message
   */
  def withLocation(location: String): MessageType =
    withHeader(Header("Location", location))
}

/** Provides HttpResponse factory methods. */
object HttpResponse {
  /** Creates an HttpResponse using the supplied attributes. */
  def apply(status: Status, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpResponse =
    SimpleHttpResponse(status, headers, body, version)

  /** Creates an HttpResponse using the supplied attributes. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    SimpleHttpResponse(statusLine.status, headers, body, statusLine.version)
}

private case class SimpleHttpResponse(status: Status, headers: Seq[Header], body: Entity, version: Version) extends HttpResponse {
  def addHeaders(moreHeaders: Header*): MessageType =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Header*): MessageType =
    copy(headers = newHeaders)

  def withBody(newBody: Entity): MessageType =
    copy(body = newBody)

  def withStatus(newStatus: Status): MessageType =
    copy(status = newStatus)

  def withVersion(newVersion: Version): MessageType =
    copy(version = newVersion)
}

