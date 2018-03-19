package scamper

import scala.util.Try

/** HTTP version */
trait HttpVersion {
  /** Version major number */
  def major: Int

  /** Version minor number */
  def minor: Int

  /** Returns formatted HTTP version. */
  override val toString: String = s"$major.$minor"
}

/** HttpVersion factory */
object HttpVersion {
  private val syntax = """(\d+)(?:\.(\d+))?""".r

  /** Parses formatted HTTP version. */
  def apply(version: String): HttpVersion =
    Try {
      version match {
        case syntax(major, null)  => HttpVersionImpl(major.toInt, 0)
        case syntax(major, minor) => HttpVersionImpl(major.toInt, minor.toInt)
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Invalid HTTP version: $version")
    }

  /** Creates HttpVersion with supplied major and minor numbers. */
  def apply(major: Int, minor: Int): HttpVersion =
    HttpVersionImpl(major, minor)

  /** Destructures HttpVersion. */
  def unapply(version: HttpVersion): Option[(Int, Int)] =
    Some((version.major, version.minor))
}

private case class HttpVersionImpl(major: Int, minor: Int) extends HttpVersion

