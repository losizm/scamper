package scamper

import Grammar._

private object HeaderHelper {
  def Key(key: String): String =
    Token.unapply(key).getOrElse {
      throw new IllegalArgumentException(s"Invalid header key: $key")
    }

  def Value(value: String): String =
    HeaderValue.unapply(value).orElse(FoldedHeaderValue.unapply(value)).getOrElse {
      throw new IllegalArgumentException(s"Invalid header value: $value")
    }
}

