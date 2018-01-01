package scamper

import scala.util.Try

/** Provides the major and minor numbers of an HTTP version. */
case class Version(major: Int, minor: Int) {
  /** Returns the version formatted as <code>major.minor</code>. */
  override def toString(): String = s"$major.$minor"
}

/** Version factory */
object Version {
  private val VersionRegex = """(\d+)\.(\d+)""".r

  /** Parses the version value. */
  def apply(value: String): Version =
    value match {
      case VersionRegex(major, minor) =>
        Try(Version(major.toInt, minor.toInt)).getOrElse {
          throw new IllegalArgumentException(s"Invalid version: $value")
        }
      case _ =>
        throw new IllegalArgumentException(s"Invalid version: $value")
    }
}

