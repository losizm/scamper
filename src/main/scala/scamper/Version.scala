package scamper

import scala.util.Try

/** HTTP version */
case class Version(major: Int, minor: Int = 0) {
  /** Returns formatted version. */
  override lazy val toString: String =
    if (minor == 0) major.toString
    else s"$major.$minor"
}

/** Version factory */
object Version {
  private val syntax = """(\d+)(?:\.(\d+))?""".r

  /** Parses formatted version. */
  def apply(version: String): Version =
    Try {
      version match {
        case syntax(major, null)  => Version(major.toInt)
        case syntax(major, minor) => Version(major.toInt, minor.toInt)
      }
    } getOrElse {
      throw new IllegalArgumentException(s"Invalid version: $version")
    }
}

