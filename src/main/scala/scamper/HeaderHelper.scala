package scamper

import Grammar._

private object HeaderHelper {
  def Key(key: String): String =
    Token(key) getOrElse {
      throw new IllegalArgumentException(s"Invalid header key: $key")
    }

  def Value(value: String): String =
    HeaderValue(value) orElse FoldedHeaderValue(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid header value: $value")
    }
}

