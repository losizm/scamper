package scamper

import Grammar._

private object CookieHelper {
  def Name(name: String): String =
    Token(name) getOrElse {
      throw new IllegalArgumentException(s"Invalid cookie name: $name")
    }

  def Value(value: String): String =
    CookieValue(value) orElse QuotedCookieValue(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid cookie value: $value")
    }
}

