package scamper

import scala.util.Try
import scala.util.matching.Regex

private trait Grammar {
  val regex: Regex

  def apply(value: String): String =
    value match {
      case regex(value) => value
      case value => throw new IllegalArgumentException()
    }

  def unapply(value: String): Option[String] =
    Try(apply(value)).toOption
}

private object Grammar {
  object Token extends Grammar {
    val regex = "([\\w!#$%&'*+.^`{}|~-]+)".r
  }

  object QuotedToken extends Grammar {
    val regex = "\"([\\w!#$%&'*+.^`{}|~-]*)\"".r
  }

  object QuotedString extends Grammar {
    val regex = "\"([\\x20-\\x7E&&[^\"]]*)\"".r
  }

  object QuotableString extends Grammar {
    val regex = "([\\x20-\\x7E&&[^\"]]*)".r
  }

  object CookieValue extends Grammar {
    val regex = "([\\x21-\\x7E&&[^\",;\\\\]]*)".r
  }

  object QuotedCookieValue extends Grammar {
    val regex = "\"([\\x21-\\x7E&&[^\",;\\\\]]*)\"".r
  }
}

