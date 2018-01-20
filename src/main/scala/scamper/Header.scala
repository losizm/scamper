package scamper

/** Provides the key-value pair of an HTTP header. */
case class Header(key: String, value: String) {
  /** Returns the formatted HTTP header. */
  override val toString: String = s"$key: $value"
}

/** Header factory */
object Header {
  private val HeaderRegex = """([\w-]+):\s*(.*)\s*""".r

  /** Parses the formatted header. */
  def apply(header: String): Header =
    header match {
      case HeaderRegex(key, value) => Header(key, value)
      case _ => throw new IllegalArgumentException(s"Invalid header: $header")
    }
}

