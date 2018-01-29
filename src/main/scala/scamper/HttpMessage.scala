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
   * If there are multiple headers for key, then the first occurrence is
   * retrieved.
   */
  def getHeader(key: String): Option[Header] =
    headers.find(_.key.equalsIgnoreCase(key))

  /** Gets all header for specified key. */
  def getHeaders(key: String): Seq[Header] =
    headers.filter(_.key.equalsIgnoreCase(key))

  /**
   * Gets header value for specified key.
   *
   * If there are multiple headers for key, then the value of first occurrence
   * is retrieved.
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
   * The value is retrieved from the Content-Type header.
   */
  def contentType: Option[ContentType] =
    getHeaderValue("Content-Type").map(ContentType.apply)

  /**
   * Gets content length.
   *
   * The value is retrieved from the Content-Length header.
   */
  def contentLength: Option[Long] =
    getHeader("Content-Length").map(_.longValue)

  /**
   * Gets content encoding.
   *
   * The value is retrieved from the Content-Encoding header.
   */
  def contentEncoding: Option[String] =
    getHeaderValue("Content-Encoding")

  /**
   * Tests whether message body is chunked.
   *
   * This is determined by inspecting the Transfer-Encoding header.
   */
  def isChunked: Boolean =
    getHeaderValue("Transfer-Encoding").exists("chunked".equalsIgnoreCase)

  /**
   * Creates a copy of this message replacing the start line.
   *
   * @return the new message
   */
  def withStartLine(line: LineType): MessageType

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
    withHeader(Header("Content-Length", length))

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
   * If chunked is true, then the Transfer-Encoding header is set to chunked;
   * otherwise, the header is removed.
   *
   * @return the new message
   */
  def withChunked(chunked: Boolean): MessageType =
    if (chunked) withHeader(Header("Transfer-Encoding", "chunked"))
    else withoutHeader("Transfer-Encoding")
}

