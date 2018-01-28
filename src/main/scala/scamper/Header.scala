package scamper

/** Provides the key-value pair of an HTTP header. */
case class Header private(key: String, value: String) {
  /** Returns the formatted HTTP header. */
  override val toString: String = s"$key: $value"
}

/** Provides Header factory methods. */
object Header {
  private val HeaderRegex = s"(${Token.regex}):\\s*(.*)\\s*".r

  /** Creates a Header using supplied key and value. */
  def apply(key: String, value: String): Header = {
    if (!Token(key))
      throw new IllegalArgumentException(s"Invalid header key: $key")

    new Header(key, value)
  }

  /** Parses the formatted header. */
  def apply(header: String): Header =
    header match {
      case HeaderRegex(key, value) => new Header(key, value)
      case _ => throw new IllegalArgumentException(s"Malformed header: $header")
    }
}

