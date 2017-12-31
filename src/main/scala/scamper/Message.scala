package scamper

import java.io.File
import java.net.URI

/**
 * A representation of an HTTP message.
 *
 * @see [[Request]], [[Response]]
 */
trait Message {
  /** The message start line */ 
  def startLine: StartLine

  /** The sequence of message headers */
  def headers: Seq[Header]

  /**
   * Gets header value for specified key. If there are multiple headers for key,
   * then the value of first header occurrence is retreived.
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
  def body: Entity
}

/** A representation of an HTTP request. */
trait Request extends Message {
  /** The request method (i.e., GET, POST, etc.) */
  def method: String

  /** The request URI */
  def uri: URI

  /** HTTP version of request message */
  def version: Version

  lazy val startLine: RequestLine =
    RequestLine(method, uri, version)
}

/** A representation of an HTTP response. */
trait Response extends Message {
  /** The response status */
  def status: Status

  /** HTTP version of response message */
  def version: Version

  lazy val startLine: StatusLine =
    StatusLine(version, status)
}

