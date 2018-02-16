package scamper

import java.time.OffsetDateTime

import Grammar._

private object CookieHelper {
  def Name(name: String): String =
    Token.unapply(name).getOrElse {
      throw new IllegalArgumentException(s"Invalid cookie name: $name")
    }

  def Value(value: String): String =
    CookieValue.unapply(value).orElse(QuotedCookieValue.unapply(value)).getOrElse {
      throw new IllegalArgumentException(s"Invalid cookie value: $value")
    }
}

