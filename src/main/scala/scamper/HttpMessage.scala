package scamper

import scala.util.Try

/** HTTP message */
trait HttpMessage {
  type MessageType <: HttpMessage
  type LineType <: StartLine
  type CookieType <: Cookie

  /** Message start line */
  def startLine: LineType

  /** Message headers */
  def headers: Seq[Header]

  /** Message cookies */
  def cookies: Seq[CookieType]

  /**
   * Gets header for specified key.
   *
   * If there are multiple headers for key, then first occurrence is retrieved.
   */
  def getHeader(key: String): Option[Header] =
    headers.find(_.key.equalsIgnoreCase(key))

  /**
   * Gets header value for specified key.
   *
   * If there are multiple headers for key, then value of first occurrence is
   * retrieved.
   */
  def getHeaderValue(key: String): Option[String] =
    getHeader(key).map(_.value)

  /** Gets all headers for specified key. */
  def getHeaders(key: String): Seq[Header] =
    headers.filter(_.key.equalsIgnoreCase(key))

  /** Gets all header values for specified key. */
  def getHeaderValues(key: String): Seq[String] =
    getHeaders(key).map(_.value)

  /** Gets cookie for specified name. */
  def getCookie(name: String): Option[CookieType] =
    cookies.find(_.name == name)

  /** Gets cookie value for specified name. */
  def getCookieValue(name: String): Option[String] =
    getCookie(name).map(_.value)

  /** Message body */
  def body: Entity

  /** Parses message body. */
  def parse[T](implicit bodyParser: BodyParser[T]): Try[T] =
    Try(bodyParser(this))

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
    withHeaders(headers.filterNot(_.key.equalsIgnoreCase(header.key)) :+ header : _*)

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
   * Creates new message including additional headers.
   *
   * @return new message
   */
  def addHeaders(headers: Header*): MessageType =
    withHeaders(this.headers ++ headers : _*)

  /**
   * Creates new message removing all headers having supplied keys.
   *
   * @return new message
   */
  def removeHeaders(keys: String*): MessageType =
    withHeaders(headers.filterNot(header => keys.exists(header.key.equalsIgnoreCase)) : _*)

  /**
   * Creates new message replacing cookies.
   *
   * All previous cookies are removed, and new message contains only supplied
   * cookies.
   *
   * @return new message
   */
  def withCookies(cookies: CookieType*): MessageType

  /**
   * Creates new message replacing body.
   *
   * @return new message
   */
  def withBody(body: Entity): MessageType
}

