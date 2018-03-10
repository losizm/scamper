package scamper

import scala.util.Try
import scala.util.matching.Regex

private class Grammar(syntax: Regex) {
  def apply(value: String): Option[String] =
    Try(value match { case syntax(first, _*) => first }).toOption
}

private object Grammar {
  val Token = new Grammar("([\\w!#$%&'*+.^`|~-]+)".r)
  val Token68 = new Grammar("([\\w.~/+-]+=*)".r)
  val QuotedString = new Grammar("\"([\\x20-\\x7E&&[^\"]]*)\"".r)
  val QuotableString = new Grammar("([\\x20-\\x7E&&[^\"]]*)".r)
  val CookieValue = new Grammar("([\\x21-\\x7E&&[^\",;\\\\]]*)".r)
  val QuotedCookieValue = new Grammar("\"([\\x21-\\x7E&&[^\",;\\\\]]*)\"".r)
  val HeaderValue = new Grammar("(\\p{Print}*)".r)
  val FoldedHeaderValue = new Grammar("((?:\\p{Print}*(?:\r\n|\r|\n)[ \t]+\\p{Print}*)*)".r)
}

