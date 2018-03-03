package scamper

private object ListParser {
  private val regex = """([^",]+|"[^"]*")""".r

  def apply(list: String): Seq[String] =
    regex.findAllIn(list).toSeq
}

