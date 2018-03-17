package scamper

/**
 * HTTP response
 *
 * @see [[HttpRequest]]
 */
trait HttpResponse extends HttpMessage {
  type MessageType = HttpResponse
  type LineType = StatusLine
  type CookieType = SetCookie

  /** Response status */
  def status: ResponseStatus = startLine.status

  /** HTTP version */
  def version: Version = startLine.version

  /**
   * Creates new response replacing status.
   *
   * @return new response
   */
  def withStatus(status: ResponseStatus): MessageType

  /**
   * Creates new response replacing version.
   *
   * @return new response
   */
  def withVersion(version: Version): MessageType
}

/** HttpResponse factory */
object HttpResponse {
  /** Creates HttpResponse using supplied attributes. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    HttpResponseImpl(statusLine, headers, body)

  /** Creates HttpResponse using supplied attributes. */
  def apply(status: ResponseStatus, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpResponse =
    HttpResponseImpl(StatusLine(status, version), headers, body)
}

private case class HttpResponseImpl(startLine: StatusLine, headers: Seq[Header], body: Entity) extends HttpResponse {
  lazy val cookies: Seq[SetCookie] =
    getHeaderValues("Set-Cookie").map(SetCookie(_))

  def withHeaders(newHeaders: Header*): HttpResponse =
    copy(headers = newHeaders)

  def withCookies(newCookies: SetCookie*): HttpResponse =
    copy(headers = headers.filterNot(_.key.equalsIgnoreCase("Set-Cookie")) ++ newCookies.map(c => Header("Set-Cookie", c.toString)))

  def withBody(newBody: Entity): HttpResponse =
    copy(body = newBody)

  def withStartLine(line: StatusLine) =
    copy(startLine = line)

  def withStatus(newStatus: ResponseStatus): HttpResponse =
    copy(startLine = StatusLine(newStatus, version))

  def withVersion(newVersion: Version): HttpResponse =
    copy(startLine = StatusLine(status, newVersion))
}

