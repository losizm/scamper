/*
 * Copyright 2019 Carlos Conyers
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
  /** Get parameter names. */
  def names: Seq[String]

  /**
   * Gets first value of named parameter.
   *
   * @param name parameter name
   *
   * @throws NoSuchElementException if parameter not present
   */
  def apply(name: String) = getOrElse(name, throw new NoSuchElementException(name))

  /**
   * Gets first value of named parameter if present.
   *
   * @param name parameter name
   */
  def get(name: String): Option[String]

  /**
   * Gets first value of named parameter if present, otherwise returns default
   * value.
   *
   * @param name parameter name
   * @param default default value
   */
  def getOrElse(name: String, default: => String): String = get(name).getOrElse(default)

  /**
   * Gets all values of named parameter.
   *
   * @param name parameter name
   */
  def getValues(name: String): Seq[String]

  /**
   * Tests whether query string contains named parameter.
   *
   * @param name parameter name
   */
  def contains(name: String): Boolean

  /** Tests whether query string is empty. */
  def isEmpty: Boolean

  /** Gets query string as sequence of parameters. */
  def toSeq: Seq[(String, String)]

  /** Gets query string as mapped parameters. */
  def toMap: Map[String, Seq[String]]

  /** Gets query string as mapped parameters with each having a single value. */
  def toSimpleMap: Map[String, String]
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
    else MapToQueryString(params)

  /**
   * Creates QueryString from parameters.
   *
   * @param params parameters
   */
  def apply(params: (String, String)*): QueryString =
    if (params.isEmpty) EmptyQueryString
    else SeqToQueryString(params)

  /**
   * Parses formatted query string.
   *
   * @param query formatted query string
   */
  def apply(query: String): QueryString =
    parse(query) match {
      case Nil    => EmptyQueryString
      case params => SeqToQueryString(params)
    }

  private def parse(query: String): Seq[(String, String)] =
    query.split("&").map(_.split("=")).toIndexedSeq.collect {
      case Array(name, value) if !name.isEmpty => decode(name, "UTF-8") -> decode(value, "UTF-8")
      case Array(name)        if !name.isEmpty => decode(name, "UTF-8") -> ""
    }

  private[scamper] def format(params: Map[String, Seq[String]]): String =
    params map {
      case (name, values) => format(values.map(value => name -> value))
    } mkString "&"

  private[scamper] def format(params: Seq[(String, String)]): String =
    params map {
      case (name, value) => s"${encode(name, "UTF-8")}=${encode(value, "UTF-8")}"
    } mkString "&"
}

private object EmptyQueryString extends QueryString {
  def names = Nil
  def get(name: String) = None
  def getValues(name: String) = Nil
  def contains(name: String) = false
  val isEmpty = true
  val toSeq = Nil
  val toMap = Map.empty
  val toSimpleMap = Map.empty
  override val toString = ""
}

private case class MapToQueryString(toMap: Map[String, Seq[String]]) extends QueryString {
  lazy val names = toMap.keys.toSeq
  def get(name: String) = toMap.get(name).flatMap(_.headOption)
  def getValues(name: String) = toMap.get(name).getOrElse(Nil)
  def contains(name: String) = toMap.contains(name)
  def isEmpty = toMap.isEmpty
  lazy val toSeq = toMap.toSeq.flatMap { case (name, values) => values.map(value => name -> value) }
  lazy val toSimpleMap = toMap.collect { case (name, Seq(value, _*)) => name -> value }.toMap
  override lazy val toString = QueryString.format(toMap)
}

private case class SeqToQueryString(toSeq: Seq[(String, String)]) extends QueryString {
  lazy val names = toSeq.map(_._1).distinct
  def get(name: String) = toSeq.collectFirst { case (`name`, value) => value }
  def getValues(name: String) = toSeq.collect { case (`name`, value) => value }
  def contains(name: String) = toSeq.exists(_._1 == name)
  def isEmpty = toSeq.isEmpty
  lazy val toMap = toSeq.groupBy(_._1).collect { case (name, params) => name -> params.map(_._2) }.toMap
  lazy val toSimpleMap = toSeq.groupBy(_._1).collect { case (name, params) => name -> params.head._2 }.toMap
  override lazy val toString = QueryString.format(toSeq)
}
