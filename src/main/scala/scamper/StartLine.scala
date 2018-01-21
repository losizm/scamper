package scamper

import scala.util.Try

/**
 * The start line of an HTTP message.
 *
 * @see [[RequestLine]], [[StatusLine]]
 */
sealed trait StartLine {
  /** HTTP version */
  def version: Version
}

/** Provides the attributes of an HTTP request line. */
case class RequestLine(method: String, uri: String, version: Version) extends StartLine {
  /** Returns the formatted HTTP request line. */
  override val toString: String = s"$method $uri HTTP/$version"
}

/** Provides RequestLine factory methods. */
object RequestLine {
  private val LineRegex = """(\w+)\h+(\p{Graph}+)\h+HTTP/(\d+\.\d+)\h*""".r

  /** Parses the formatted request line. */
  def apply(line: String): RequestLine =
    line match {
      case LineRegex(method, uri, version) =>
        Try(RequestLine(method, uri, Version(version))).getOrElse {
          throw new IllegalArgumentException(s"Invalid request line: $line")
        }
      case _ =>
        throw new IllegalArgumentException(s"Invalid request line: $line")
    }
}

/** Provides the attributes of an HTTP status line. */
case class StatusLine(version: Version, status: Status) extends StartLine {
  /** Returns the formatted HTTP status line. */
  override val toString: String = s"HTTP/$version ${status.code} ${status.reason}"
}

/** Provides StatusLine factory methods. */
object StatusLine {
  private val LineRegex = """HTTP/(\d+\.\d+)\h+(\d+)\h+(\p{Print}+)\h*""".r

  /** Parses the formatted status line. */
  def apply(line: String): StatusLine =
    line match {
      case LineRegex(version, code, reason) =>
        Try(StatusLine(Version(version), Status(code.toInt, reason))).getOrElse {
          throw new IllegalArgumentException(s"Invalid response line: $line")
        }
      case _ =>
        throw new IllegalArgumentException(s"Invalid response line: $line")
    }
}

