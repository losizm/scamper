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
  override lazy val toString: String = s"$method $uri HTTP/$version"
}

/** RequestLine factory */
object RequestLine {
  private val syntax = """(\w+)[ \t]+(\p{Graph}+)[ \t]+HTTP/(\d+(?:\.\d+)?)[ \t]*""".r

  /** Parses formatted request line. */
  def apply(line: String): RequestLine =
    Try {
      line match {
        case syntax(method, uri, version) => RequestLine(method, uri, Version(version))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed request line: $line")
    }
}

/** HTTP status line */
case class StatusLine(version: Version, status: Status) extends StartLine {
  /** Returns formatted HTTP status line. */
  override lazy val toString: String = s"HTTP/$version ${status.code} ${status.reason}"
}

/** StatusLine factory */
object StatusLine {
  private val syntax = """HTTP/(\d+(?:\.\d+)?)[ \t]+(\d+)[ \t]+(\p{Print}+)[ \t]*""".r

  /** Parses formatted status line. */
  def apply(line: String): StatusLine =
    Try {
      line match {
        case syntax(version, code, reason) => StatusLine(Version(version), Status(code.toInt, reason))
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Malformed status line: $line")
    }
}

