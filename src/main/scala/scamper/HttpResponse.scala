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
   * Value retrieved from Date header.
   */
  def date: Option[OffsetDateTime] =
    getHeader("Date").map(_.dateValue)

  /**
   * Gets entity tag.
   *
   * Value retrieved from ETag header.
   */
  def entityTag: Option[String] =
   getHeaderValue("ETag")

  /**
   * Gets location.
   *
   * Value retrieved from Location header.
   */
  def location: Option[String] =
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
   * Creates new response replacing date.
   *
   * @return new response
   */
  def withDate(date: OffsetDateTime): MessageType =
    withHeader(Header("Date", date))

  /**
   * Creates new message replacing entity tag.
   *
   * @return new response
   */
  def withEntityTag(tag: String): MessageType =
    withHeader(Header("ETag", tag))

  /**
   * Creates new message replacing location.
   *
   * @return new response
   */
  def withLocation(location: String): MessageType =
    withHeader(Header("Location", location))
}

/** Provides HttpResponse factory methods. */
object HttpResponse {
  /** Creates HttpResponse using supplied attributes. */
  def apply(statusLine: StatusLine, headers: Seq[Header], body: Entity): HttpResponse =
    SimpleHttpResponse(statusLine, headers, body)

  /** Creates HttpResponse using supplied attributes. */
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

