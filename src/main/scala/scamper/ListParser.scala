package scamper

import scala.util.matching.Regex

private object ListParser {
  private val `,` = """([^",]+|"[^"]*")+""".r
  private val `;` = """([^";]+|"[^"]*")+""".r

  def apply(list: String): Seq[String] =
    split(`,`, list)

  def apply(list: String, semicolon: Boolean): Seq[String] =
    split(if (semicolon) `;` else `,`, list)

  private def split(regex: Regex, list: String): Seq[String] =
    regex.findAllIn(list).map(_.trim).toSeq
}

