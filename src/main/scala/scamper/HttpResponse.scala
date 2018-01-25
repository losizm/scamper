package scamper

/** A representation of an HTTP response. */
trait HttpResponse extends HttpMessage {
  type MessageType = HttpResponse
  type LineType = StatusLine

  /** Response status */
  def status: Status = startLine.status

  /** HTTP version of response message */
  def version: Version = startLine.version

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
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    SimpleHttpResponse(statusLine, headers, body)

  /** Creates an HttpResponse using the supplied attributes. */
  def apply(status: Status, headers: Seq[Header] = Nil, body: Entity = Entity.empty, version: Version = Version(1, 1)): HttpResponse =
    SimpleHttpResponse(StatusLine(version, status), headers, body)
}

private case class SimpleHttpResponse(startLine: StatusLine, headers: Seq[Header], body: Entity) extends HttpResponse {
  def addHeaders(moreHeaders: Header*): HttpResponse =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Header*): HttpResponse =
    copy(headers = newHeaders)

  def withBody(newBody: Entity): HttpResponse =
    copy(body = newBody)

  def withStartLine(line: StatusLine) =
    copy(startLine = line)

  def withStatus(newStatus: Status): HttpResponse =
    copy(startLine = startLine.copy(status = newStatus))

  def withVersion(newVersion: Version): HttpResponse =
    copy(startLine = startLine.copy(version = newVersion))
}

