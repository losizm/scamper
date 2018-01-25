package scamper

private object QueryParser {
  import bantam.nx.lang.StringType

  def parse(query: String): Map[String, List[String]] =
    query.split("&").map(_.split("=")).collect {
      case Array(name, value) if !name.isEmpty => name.toURLDecoded -> value.toURLDecoded
      case Array(name)        if !name.isEmpty => name.toURLDecoded -> ""
    }.groupBy(_._1).map {
      case (name, value) => name -> value.map(_._2).toList
    }

  def format(params: Map[String, List[String]]): String =
    params.toSeq.map {
      case (name, values) => format(values.map(name -> _) : _*)
    }.mkString("&")

  def format(params: (String, String)*): String =
    params.map {
      case (name, value) => s"${name.toURLEncoded}=${value.toURLEncoded}"
    }.mkString("&")
}


