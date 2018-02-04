package scamper

import scala.util.Try

/**
 * Representation of HTTP message.
 *
 * @see [[HttpRequest]], [[HttpResponse]]
 */
trait HttpMessage {
  type MessageType <: HttpMessage
  type LineType <: StartLine

  /** Message start line */
  def startLine: LineType

  /** Sequence of message headers */
  def headers: Seq[Header]

  /**
   * Gets header for specified key.
   *
   * If there are multiple headers for key, then first occurrence is retrieved.
   *
   * @throws HeaderNotFound if no header with specified key is present
   *
   * @see [[getHeader]]
   */
  def header(key: String): Header =
    getHeader(key).getOrElse(throw HeaderNotFound(key))

  /**
   * Gets header value for specified key.
   *
   * If there are multiple headers for key, then value of first occurrence is
   * retrieved.
   *
   * @throws HeaderNotFound if no header with specified key is present
   *
   * @see [[getHeaderValue]]
   */
  def headerValue(key: String): String =
    getHeaderValue(key).getOrElse(throw HeaderNotFound(key))

  /**
   * Gets header for specified key.
   *
   * If there are multiple headers for key, then first occurrence is retrieved.
   *
   * @see [[header]]
   */
  def getHeader(key: String): Option[Header] =
    headers.find(_.key.equalsIgnoreCase(key))

  /** Gets all header for specified key. */
  def getHeaders(key: String): Seq[Header] =
    headers.filter(_.key.equalsIgnoreCase(key))

  /**
   * Gets header value for specified key.
   *
   * If there are multiple headers for key, then value of first occurrence is
   * retrieved.
   *
   * @see [[headerValue]]
   */
  def getHeaderValue(key: String): Option[String] =
    getHeader(key).map(_.value)

  /** Gets all header values for specified key. */
  def getHeaderValues(key: String): List[String] =
    getHeaders(key).map(_.value).toList

  /** Message body */
  def body: Entity

  /** Parses message body. */
  def parse[T](implicit bodyParser: BodyParser[T]): Try[T] =
    Try(bodyParser(this))

  /**
   * Gets content type.
   *
   * Value retrieved from Content-Type header.
   */
  def contentType: Option[ContentType] =
    getHeaderValue("Content-Type").map(ContentType.apply)

  /**
   * Gets content length.
   *
   * Value retrieved from Content-Length header.
   */
  def contentLength: Option[Long] =
    getHeader("Content-Length").map(_.longValue)

  /**
   * Gets content encoding.
   *
   * Value retrieved from Content-Encoding header.
   */
  def contentEncoding: Option[String] =
    getHeaderValue("Content-Encoding")

  /**
   * Tests whether message body is chunked.
   *
   * Value determined by inspecting Transfer-Encoding header.
   */
  def isChunked: Boolean =
    getHeaderValue("Transfer-Encoding").exists("chunked".equalsIgnoreCase)

  /**
   * Creates new message replacing start line.
   *
   * @return new message
   */
  def withStartLine(line: LineType): MessageType

  /**
   * Creates new message replacing supplied header.
   *
   * All previous headers having same key as supplied header are removed and
   * replaced with single header instance.
   *
   * @return new message
   */
  def withHeader(header: Header): MessageType =
    withHeaders {
      headers.filterNot(_.key.equalsIgnoreCase(header.key)) :+ header : _*
    }

  /**
   * Creates new message removing all headers having supplied key.
   *
   * @return new message
   */
  def withoutHeader(key: String): MessageType =
    withHeaders {
      headers.filterNot(_.key.equalsIgnoreCase(key)) : _*
    }

  /**
   * Creates new message including additional headers.
   *
   * @return new message
   */
  def addHeaders(headers: Header*): MessageType

  /**
   * Creates new message replacing headers.
   *
   * All previous headers are removed, and new message contains only supplied
   * headers.
   *
   * @return new message
   */
  def withHeaders(headers: Header*): MessageType

  /**
   * Creates new message replacing body.
   *
   * @return new message
   */
  def withBody(body: Entity): MessageType

  /**
   * Creates new message replacing content type.
   *
   * @return new message
   */
  def withContentType(contentType: ContentType): MessageType =
    withHeader(Header("Content-Type", contentType.toString))

  /**
   * Creates new message replacing content length.
   *
   * @return new message
   */
  def withContentLength(length: Long): MessageType =
    withHeader(Header("Content-Length", length))

  /**
   * Creates new message replacing content encoding.
   *
   * @return new message
   */
  def withContentEncoding(encoding: String): MessageType =
    withHeader(Header("Content-Encoding", encoding))

  /**
   * Creates new message replacing transfer encoding.
   *
   * If chunked is true, then Transfer-Encoding header is set to chunked;
   * otherwise, header is removed.
   *
   * @return new message
   */
  def withChunked(chunked: Boolean): MessageType =
    if (chunked) withHeader(Header("Transfer-Encoding", "chunked"))
    else withoutHeader("Transfer-Encoding")
}

