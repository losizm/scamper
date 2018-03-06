package scamper

import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

private object QueryParams {
  def parse(query: String): Map[String, Seq[String]] =
    query.split("&").map(_.split("=")) collect {
      case Array(name, value) if !name.isEmpty => decode(name, "UTF-8") -> decode(value, "UTF-8")
      case Array(name)        if !name.isEmpty => decode(name, "UTF-8") -> ""
    } groupBy(_._1) map {
      case (name, params) => name -> params.map(_._2).toSeq
    }

  def format(params: Map[String, Seq[String]]): String =
    params map {
      case (name, values) => format(values.map(value => name -> value) : _*)
    } mkString "&"

  def format(params: (String, String)*): String =
    params map {
      case (name, value) => s"${encode(name, "UTF-8")}=${encode(value, "UTF-8")}"
    } mkString "&"
}

