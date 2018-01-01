package scamper

import java.net.URI
import scala.util.Try

/**
 * A representation of an HTTP message.
 *
 * @see [[HttpRequest]], [[HttpResponse]]
 */
trait HttpMessage {
  type MessageType <: HttpMessage

  /** The message start line */
  def startLine: StartLine

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
      case Header(k, value) if k.toLowerCase == key.toLowerCase => value
    }

  /** Gets all header values for specified key. */
  def getHeaderValues(key: String): List[String] =
    headers.collect {
      case Header(k, value) if k.toLowerCase == key.toLowerCase => value
    }.toList

  /** The message body */
  def body: Option[Entity]

  /** Parses the message body. */
  def parse[T]()(implicit bodyParser: BodyParser[T]): Try[T] =
    bodyParser(this)

  /**
   * Creates a copy of this message with the additional header.
   *
   * @return the new message
   */
  def addHeader(header: Header): MessageType

  /**
   * Creates a copy of this message with the additional headers.
   *
   * @return the new message
   */
  def addHeaders(headers: Seq[Header]): MessageType

  /**
   * Creates a copy of this message with a new sequence of headers.
   *
   * @return the new message
   */
  def withHeaders(headers: Seq[Header]): MessageType

  /**
   * Creates a copy of this message with a new body.
   *
   * @return the new message
   */
  def withBody(body: Option[Entity]): MessageType
}

/** A representation of an HTTP request. */
trait HttpRequest extends HttpMessage {
  type MessageType = HttpRequest

  /** The request method (i.e., GET, POST, etc.) */
  def method: String

  /** The request URI */
  def uri: URI

  /** HTTP version of request message */
  def version: Version

  lazy val startLine: RequestLine =
    RequestLine(method, uri, version)

  /**
   * Creates a copy of this request with a new method.
   *
   * @return the new request
   */
  def withMethod(method: String): MessageType

  /**
   * Creates a copy of this request with a new URI.
   *
   * @return the new request
   */
  def withURI(uri: URI): MessageType

  /**
   * Creates a copy of this request with a new version.
   *
   * @return the new request
   */
  def withVersion(version: Version): MessageType
}

/** HttpRequest factory */
object HttpRequest {
  /** Creates an HttpRequest using the supplied attributes. */
  def apply(method: String, uri: URI, headers: Seq[Header] = Nil, body: Option[Entity] = None, version: Version = Version(1, 1)): HttpRequest =
    SimpleHttpRequest(method, uri, headers, body, version)
}

private case class SimpleHttpRequest(method: String, uri: URI, headers: Seq[Header], body: Option[Entity], version: Version) extends HttpRequest {
  def addHeader(header: Header): MessageType =
    copy(headers = headers :+ header)

  def addHeaders(moreHeaders: Seq[Header]): MessageType =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Seq[Header]): MessageType =
    copy(headers = newHeaders)

  def withBody(newBody: Option[Entity]): MessageType =
    copy(body = newBody)

  def withMethod(newMethod: String): MessageType =
    copy(method = newMethod)

  def withURI(newURI: URI): MessageType =
    copy(uri = newURI)

  def withVersion(newVersion: Version): MessageType =
    copy(version = newVersion)
}

/** A representation of an HTTP response. */
trait HttpResponse extends HttpMessage {
  type MessageType = HttpResponse

  /** The response status */
  def status: Status

  /** HTTP version of response message */
  def version: Version

  lazy val startLine: StatusLine =
    StatusLine(version, status)

  /**
   * Creates a copy of this response with a new status.
   *
   * @return the new response
   */
  def withStatus(status: Status): MessageType

  /**
   * Creates a copy of this response with a new version.
   *
   * @return the new response
   */
  def withVersion(version: Version): MessageType
}

/** HttpResponse factory */
object HttpResponse {
  /** Creates an HttpResponse using the supplied attributes. */
  def apply(status: Status, headers: Seq[Header] = Nil, body: Option[Entity] = None, version: Version = Version(1, 1)): HttpResponse =
    SimpleHttpResponse(status, headers, body, version)
}

private case class SimpleHttpResponse(status: Status, headers: Seq[Header], body: Option[Entity], version: Version) extends HttpResponse {
  def addHeader(header: Header): MessageType =
    copy(headers = headers :+ header)

  def addHeaders(moreHeaders: Seq[Header]): MessageType =
    copy(headers = headers ++ moreHeaders)

  def withHeaders(newHeaders: Seq[Header]): MessageType =
    copy(headers = newHeaders)

  def withBody(newBody: Option[Entity]): MessageType =
    copy(body = newBody)

  def withStatus(newStatus: Status): MessageType =
    copy(status = newStatus)

  def withVersion(newVersion: Version): MessageType =
    copy(version = newVersion)
}

