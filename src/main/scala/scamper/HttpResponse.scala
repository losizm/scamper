package scamper

/** HTTP response */
trait HttpResponse extends HttpMessage {
  type MessageType = HttpResponse
  type LineType = StatusLine
  type CookieType = SetCookie

  /** Response status */
  def status: Status = startLine.status

  /** HTTP version of response message */
  def version: Version = startLine.version

  /**
   * Gets all response cookies.
   *
   * Values retrieved from Set-Cookie headers.
   */
  lazy val cookies: Seq[SetCookie] =
    getHeaderValues("Set-Cookie").map(SetCookie.apply)

  /**
   * Gets location.
   *
   * Value retrieved from Location header.
   */
  lazy val location: Option[String] =
    getHeaderValue("Location")

  /**
   * Creates new response replacing status.
   *
   * @return new response
   */
  def withStatus(status: Status): MessageType

  /**
   * Creates new response replacing version.
   *
   * @return new response
   */
  def withVersion(version: Version): MessageType

  /**
   * Creates new response replacing location.
   *
   * @return new response
   */
  def withLocation(location: String): MessageType =
    withHeader(Header("Location", location))
}

/** HttpResponse factory */
object HttpResponse {
  /** Creates HttpResponse using supplied attributes. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    SimpleHttpResponse(statusLine, headers, body)

  /** Creates HttpResponse using supplied attributes. */
  def apply(status: Status, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpResponse =
    SimpleHttpResponse(StatusLine(version, status), headers, body)
}

private case class SimpleHttpResponse(startLine: StatusLine, headers: Seq[Header], body: Entity) extends HttpResponse {
  def addHeaders(newHeaders: Header*): HttpResponse =
    copy(headers = headers ++ newHeaders)

  def withHeaders(newHeaders: Header*): HttpResponse =
    copy(headers = newHeaders)

  def withCookies(newCookies: SetCookie*): HttpResponse =
    copy(headers = headers.filterNot(_.key.equalsIgnoreCase("Set-Cookie")) ++ newCookies.map(c => Header("Set-Cookie", c.toString)))

  def withBody(newBody: Entity): HttpResponse =
    copy(body = newBody)

  def withStartLine(line: StatusLine) =
    copy(startLine = line)

  def withStatus(newStatus: Status): HttpResponse =
    copy(startLine = startLine.copy(status = newStatus))

  def withVersion(newVersion: Version): HttpResponse =
    copy(startLine = startLine.copy(version = newVersion))
}

