/*
 * Copyright 2018 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper

import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

/** Represents query string as mapped parameters. */
trait QueryString {
  /** Gets first value of named parameter. */
  def getValue(name: String): Option[String]

  /** Gets all values of named parameter. */
  def getValues(name: String): Seq[String]

  /** Gets query string as mapped parameters. */
  def toMap: Map[String, Seq[String]]

  /** Gets query string as mapped parameters with each having a single value. */
  def toSimpleMap: Map[String, String]

  /** Gets URL encoded query string. */
  override lazy val toString: String = QueryString.format(toMap)
}

/** Provides factory methods for QueryString. */
object QueryString {
  /** Gets empty query string. */
  def empty: QueryString = EmptyQueryString

  /**
   * Creates QueryString from parameters.
   *
   * @param params parameters
   */
  def apply(params: Map[String, Seq[String]]): QueryString =
    if (params.isEmpty) EmptyQueryString
    else QueryStringImpl(params)

  /**
   * Creates QueryString from parameters.
   *
   * @param params parameters
   */
  def apply(params: (String, String)*): QueryString =
   apply(params.map { case (name, value) => name -> Seq(value) }.toMap)

  /**
   * Parses formatted query string.
   *
   * @param query formatted query string
   */
  def apply(query: String): QueryString =
    apply(parse(query))

  private[scamper] def parse(query: String): Map[String, Seq[String]] =
    query.split("&").map(_.split("=")) collect {
      case Array(name, value) if !name.isEmpty => decode(name, "UTF-8") -> decode(value, "UTF-8")
      case Array(name)        if !name.isEmpty => decode(name, "UTF-8") -> ""
    } groupBy(_._1) map {
      case (name, params) => name -> params.map(_._2).toSeq
    }

  private[scamper] def format(params: Map[String, Seq[String]]): String =
    params map {
      case (name, values) => format(values.map(value => name -> value) : _*)
    } mkString "&"

  private[scamper] def format(params: (String, String)*): String =
    params map {
      case (name, value) => s"${encode(name, "UTF-8")}=${encode(value, "UTF-8")}"
    } mkString "&"
}

private object EmptyQueryString extends QueryString {
  def getValue(name: String) = None
  def getValues(name: String) = Nil
  val toMap = Map.empty
  val toSimpleMap = Map.empty
}

private case class QueryStringImpl(toMap: Map[String, Seq[String]]) extends QueryString {
  def getValue(name: String) = toMap.get(name).flatMap(_.headOption)
  def getValues(name: String) = toMap.get(name).getOrElse(Nil)
  lazy val toSimpleMap = toMap.collect { case (name, Seq(value, _*)) => name -> value }.toMap
}
