package scamper

import scala.util.Try

/** HTTP version */
trait Version {
  /** Version major number */
  def major: Int

  /** Version minor number */
  def minor: Int

  /** Returns formatted version. */
  override val toString: String = s"$major.$minor"
}

/** Version factory */
object Version {
  private val syntax = """(\d+)(?:\.(\d+))?""".r

  /** Parses formatted version. */
  def apply(version: String): Version =
    Try {
      version match {
        case syntax(major, null)  => new VersionImpl(major.toInt, 0)
        case syntax(major, minor) => new VersionImpl(major.toInt, minor.toInt)
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Invalid version: $version")
    }

  /** Creates Version with supplied major and minor numbers. */
  def apply(major: Int, minor: Int): Version =
    new VersionImpl(major, minor)

  /** Destructures Version. */
  def unapply(version: Version): Option[(Int, Int)] =
    Some((version.major, version.minor))
}

private class VersionImpl(val major: Int, val minor: Int) extends Version {
  override def equals(that: Any): Boolean =
    that match {
      case Version(major, minor) => this.major == major && this.minor == minor
      case _ => false
    }
}
