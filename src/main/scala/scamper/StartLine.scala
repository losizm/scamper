package scamper

import scala.util.Try

/**
 * Start line of HTTP message
 *
 * @see [[RequestLine]], [[StatusLine]]
 */
sealed trait StartLine {
  /** HTTP version */
  def version: Version
}

/** HTTP request line */
case class RequestLine(method: String, uri: String, version: Version) extends StartLine {
  /** Returns formatted HTTP request line. */
  override val toString: String = s"$method $uri HTTP/$version"
}

/** RequestLine factory */
object RequestLine {
  private val LineRegex = """(\w+)\h+(\p{Graph}+)\h+HTTP/(\d+\.\d+)\h*""".r

  /** Parses formatted request line. */
  def apply(line: String): RequestLine =
    line match {
      case LineRegex(method, uri, version) =>
        Try(RequestLine(method, uri, Version(version))) getOrElse {
          throw new IllegalArgumentException(s"Malformed request line: $line")
        }
      case _ =>
        throw new IllegalArgumentException(s"Malformed request line: $line")
    }
}

/** HTTP status line */
case class StatusLine(version: Version, status: Status) extends StartLine {
  /** Returns formatted HTTP status line. */
  override val toString: String = s"HTTP/$version ${status.code} ${status.reason}"
}

/** StatusLine factory */
object StatusLine {
  private val LineRegex = """HTTP/(\d+\.\d+)\h+(\d+)\h+(\p{Print}+)\h*""".r

  /** Parses formatted status line. */
  def apply(line: String): StatusLine =
    line match {
      case LineRegex(version, code, reason) =>
        Try(StatusLine(Version(version), Status(code.toInt, reason))) getOrElse {
          throw new IllegalArgumentException(s"Malformed status line: $line")
        }
      case _ =>
        throw new IllegalArgumentException(s"Malformed status line: $line")
    }
}

