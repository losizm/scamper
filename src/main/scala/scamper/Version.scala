package scamper

import bantam.nx.lang.DefaultType

import scala.util.Try

/** HTTP version */
case class Version(major: Int, minor: Int) {
  /** Returns formatted version. */
  override val toString: String =
    if (minor == 0) major.toString
    else s"$major.$minor"
}

/** Version factory */
object Version {
  private val VersionRegex = """(\d+)(?:\.(\d+))?""".r

  /** Parses formatted version. */
  def apply(version: String): Version =
    version match {
      case VersionRegex(major, minor) =>
        Try(Version(major.toInt, (minor ?: "0").toInt)).getOrElse {
          throw new IllegalArgumentException(s"Invalid version: $version")
        }
      case _ =>
        throw new IllegalArgumentException(s"Invalid version: $version")
    }
}

