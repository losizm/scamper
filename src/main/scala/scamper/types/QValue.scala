package scamper.types

private object QValue {
  val key = "([Qq])".r
  val value = "(\\d+(?:\\.\\d*))".r

  def apply(value: Float): Float =
    (value.max(0f).min(1f) * 1000).floor / 1000
}

