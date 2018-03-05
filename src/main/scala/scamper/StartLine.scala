package scamper

import scala.util.Try

/** Start line of HTTP message */
trait StartLine {
  /** HTTP version */
  def version: Version
}

/** HTTP request line */
trait RequestLine extends StartLine {
  /** Request method */
  def method: String

  /** Request URI */
  def uri: String

  /** HTTP version */
  def version: Version

  /** Returns formatted request line. */
  override lazy val toString: String = s"$method $uri HTTP/$version"
}

/** RequestLine factory */
object RequestLine {
  private val syntax = """(\w+)[ \t]+(\p{Graph}+)[ \t]+HTTP/(\d+(?:\.\d+)?)[ \t]*""".r

  /** Parses formatted request line. */
  def apply(line: String): RequestLine =
    Try {
      line match {
        case syntax(method, uri, version) => new RequestLineImpl(method.toUpperCase, uri, Version(version))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed request line: $line")
    }

  /** Creates RequestLine with supplied attributes. */
  def apply(method: String, uri: String, version: Version = Version(1, 1)): RequestLine =
    new RequestLineImpl(method.toUpperCase, uri, version)

  /** Destructures RequestLine. */
  def unapply(line: RequestLine): Option[(String, String, Version)] =
    Some((line.method, line.uri, line.version))
}

private class RequestLineImpl(val method: String, val uri: String, val version: Version) extends RequestLine

/** HTTP status line */
trait StatusLine extends StartLine {
  /** Response status */
  def status: Status

  /** Response version */
  def version: Version

  /** Returns formatted status line. */
  override lazy val toString: String = s"HTTP/$version ${status.code} ${status.reason}"
}

/** StatusLine factory */
object StatusLine {
  private val syntax = """HTTP/(\d+(?:\.\d+)?)[ \t]+(\d+)[ \t]+(\p{Print}+)[ \t]*""".r

  /** Parses formatted status line. */
  def apply(line: String): StatusLine =
    Try {
      line match {
        case syntax(version, code, reason) => new StatusLineImpl(Status(code.toInt, reason), Version(version))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed status line: $line")
    }

  /** Creates StatusLine with supplied attributes. */
  def apply(status: Status, version: Version = Version(1, 1)): StatusLine =
    new StatusLineImpl(status, version)

  /** Destructures StatusLine. */
  def unapply(line: StatusLine): Option[(Status, Version)] =
    Some((line.status, line.version))
}

private class StatusLineImpl(val status: Status, val version: Version) extends StatusLine
