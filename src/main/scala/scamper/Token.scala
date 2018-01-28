package scamper

private object Token {
  val regex: String = "[\\w!#$%&'*+.^`{}|~-]+"

  def apply(s: String): Boolean = {
    if (s == null) false
    else s.matches(regex)
  }
}

