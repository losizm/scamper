package scamper

/**
 * HTTP request
 *
 * @see [[HttpResponse]]
 */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest
  type LineType = RequestLine
  type CookieType = PlainCookie

  /** Request method */
  def method: String = startLine.method

  /** Request URI */
  def uri: String = startLine.uri

  /** HTTP version */
  def version: Version = startLine.version

  /**
   * Gets all request cookies.
   *
   * Values retrieved from Cookie header.
   */
  lazy val cookies: Seq[PlainCookie] =
    getHeaderValue("Cookie")
      .map(_.split("\\s*;\\s*"))
      .map(_.map(PlainCookie.apply).toSeq)
      .getOrElse(Nil)

  /**
   * Creates new request replacing method.
   *
   * @return new request
   */
  def withMethod(method: String): MessageType

  /**
   * Creates new request replacing URI.
   *
   * @return new request
   */
  def withURI(uri: String): MessageType

  /**
   * Creates new request replacing version.
   *
   * @return new request
   */
  def withVersion(version: Version): MessageType
}

/** HttpRequest factory */
object HttpRequest {
  /** Creates HttpRequest using supplied attributes. */
  def apply(requestLine: RequestLine, headers: Seq[Header], body: Entity): HttpRequest =
    HttpRequestImpl(requestLine, headers, body)

  /** Creates HttpRequest using supplied attributes. */
  def apply(method: String, uri: String = "/", headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpRequest =
    HttpRequestImpl(RequestLine(method, uri, version), headers, body)
}

private case class HttpRequestImpl(startLine: RequestLine, headers: Seq[Header], body: Entity) extends HttpRequest {
  def addHeaders(newHeaders: Header*): HttpRequest =
    copy(headers = headers ++ newHeaders)

  def withHeaders(newHeaders: Header*): HttpRequest =
    copy(headers = newHeaders)

  def withCookies(newCookies: PlainCookie*): HttpRequest =
    copy(headers = headers.filterNot(_.key.equalsIgnoreCase("Cookie")) :+ Header("Cookie", newCookies.mkString("; ")))

  def withBody(newBody: Entity): HttpRequest =
    copy(body = newBody)

  def withStartLine(newStartLine: RequestLine): HttpRequest =
    copy(startLine = newStartLine)

  def withMethod(newMethod: String): HttpRequest =
    copy(startLine = startLine.copy(method = newMethod))

  def withURI(newURI: String): HttpRequest =
    copy(startLine = startLine.copy(uri = newURI))

  def withVersion(newVersion: Version): HttpRequest =
    copy(startLine = startLine.copy(version = newVersion))
}

