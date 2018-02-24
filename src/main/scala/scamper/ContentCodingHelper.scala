package scamper

import java.text.DecimalFormat

import Grammar._

private object ContentCodingHelper {
  def Name(name: String): String =
    Token.unapply(name).getOrElse {
      throw new IllegalArgumentException(s"Invalid name: $name")
    }

  def QValue(qvalue: Float): Float =
    if (qvalue >= 0.0f && qvalue <= 1.0f) qvalue
    else throw new IllegalArgumentException(s"qvalue out of range: $qvalue")
}

