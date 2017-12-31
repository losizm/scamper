package scamper

/** Provides the key-value pair of an HTTP header. */
case class Header(key: String, value: String) {
  /** Returns a canonically formatted HTTP header. */
  override def toString(): String = s"$key: $value"
}

/** Header factory */
object Header {
  private val HeaderRegex = """([\w-]+):\s*(.*)\s*""".r

  /** Parses header line. */
  def apply(line: String): Header =
    line match {
      case HeaderRegex(key, value) =>
        Header(key, value)
      case _ =>
        throw new IllegalArgumentException(s"Invalid header line: $line")
    }
}

