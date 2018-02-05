package scamper

private object QueryParser {
  import bantam.nx.lang.StringType

  def parse(query: String): Map[String, Seq[String]] =
    query.split("&").map(_.split("=")) collect {
      case Array(name, value) if !name.isEmpty => name.toURLDecoded -> value.toURLDecoded
      case Array(name)        if !name.isEmpty => name.toURLDecoded -> ""
    } groupBy(_._1) map {
      case (name, value) => name -> value.map(_._2).toSeq
    }

  def format(params: Map[String, Seq[String]]): String =
    params map {
      case (name, values) => format(values.map(name -> _) : _*)
    } mkString "&"

  def format(params: (String, String)*): String =
    params map {
      case (name, value) => s"${name.toURLEncoded}=${value.toURLEncoded}"
    } mkString "&"
}


