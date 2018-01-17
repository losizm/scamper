package scamper

import scala.util.Try

/** Provides the major and minor numbers of an HTTP version. */
case class Version(major: Int, minor: Int) {
  /** Returns the formatted version. */
  override val toString: String = s"$major.$minor"
}

/** Version factory */
object Version {
  private val VersionRegex = """(\d+)\.(\d+)""".r

  /** Parses the version. */
  def apply(version: String): Version =
    version match {
      case VersionRegex(major, minor) =>
        Try(Version(major.toInt, minor.toInt)).getOrElse {
          throw new IllegalArgumentException(s"Invalid version: $version")
        }
      case _ =>
        throw new IllegalArgumentException(s"Invalid version: $version")
    }
}

