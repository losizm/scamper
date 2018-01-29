package scamper

import java.time.OffsetDateTime

/** Representation of HTTP response. */
trait HttpResponse extends HttpMessage {
  type MessageType = HttpResponse
  type LineType = StatusLine

  /** Response status */
  def status: Status = startLine.status

  /** HTTP version of response message */
  def version: Version = startLine.version

  /**
   * Gets response date.
   *
   * The value is retrieved from the Date header.
   */
  def date: Option[OffsetDateTime] =
    getHeader("Date").map(_.dateValue)

  /**
   * Gets entity tag.
   *
   * The value is retrieved from the ETag header.
   */
  def entityTag: Option[String] =
   getHeaderValue("ETag")

  /**
   * Gets location.
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
   * Creates a copy of this response replacing the response date.
   *
   * @return the new response
   */
  def withDate(date: OffsetDateTime): MessageType =
    withHeader(Header("Date", date))

  /**
   * Creates a copy of this message replacing the entity tag.
   *
   * @return the new response
   */
  def withEntityTag(tag: String): MessageType =
    withHeader(Header("ETag", tag))

  /**
   * Creates a copy of this message replacing the location.
   *
   * @return the new response
   */
  def withLocation(location: String): MessageType =
    withHeader(Header("Location", location))
}

/** Provides HttpResponse factory methods. */
object HttpResponse {
  /** Creates an HttpResponse using supplied attributes. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    SimpleHttpResponse(statusLine, headers, body)

  /** Creates an HttpResponse using supplied attributes. */
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

