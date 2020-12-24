/*
 * Copyright 2017-2020 Carlos Conyers
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
  /** Gets parameter names. */
  def names: Seq[String]

  /**
   * Gets first parameter value with given name.
   *
   * @param name parameter name
   *
   * @throws NoSuchElementException if parameter not present
   */
  def apply(name: String) = getOrElse(name, throw new NoSuchElementException(name))

  /**
   * Gets first parameter value with given name if present.
   *
   * @param name parameter name
   */
  def get(name: String): Option[String]

  /**
   * Gets first parameter value with given name if present; otherwise, returns
   * default value.
   *
   * @param name parameter name
   * @param default default value
   */
  def getOrElse(name: String, default: => String): String = get(name).getOrElse(default)

  /**
   * Gets first parameter value with given name and parses it to `Int` if
   * present.
   *
   * @param name parameter name
   *
   * @throws NumberFormatException if parameter value cannot be parsed to `Int`
   */
  def getInt(name: String): Option[Int] =
    get(name).map(_.toInt)

  /**
   * Gets first parameter value with given name and parses it to `Long` if
   * present.
   *
   * @param name parameter name
   *
   * @throws NumberFormatException if parameter value cannot be parsed to `Long`
   */
  def getLong(name: String): Option[Long] =
    get(name).map(_.toLong)

  /**
   * Gets parameter values with given name.
   *
   * @param name parameter name
   *
   * @note If parameter is not present, an empty sequence is returned.
   */
  def getValues(name: String): Seq[String]

  /**
   * Tests for parameter with given name.
   *
   * @param name parameter name
   */
  def contains(name: String): Boolean

  /** Tests for empty. */
  def isEmpty: Boolean

  /**
   * Adds supplied values to parameter with given name.
   *
   * If a parameter with given name already exists, the newly supplied values
   * are appended to the existing values.
   *
   * If a parameter does not exist, it is added with supplied values.
   *
   * @param name parameter name
   * @param values parameter values
   *
   * @return new query string
   */
  def add(name: String, values: Seq[String]): QueryString

  /**
   * Adds supplied values to parameter with given name.
   *
   * If a parameter with given name already exists, the newly supplied values
   * are appended to the existing values.
   *
   * If a parameter does not exist, it is added with supplied values.
   *
   * @param name parameter name
   * @param one parameter value
   * @param more additional parameter values
   *
   * @return new query string
   */
  def add(name: String, one: String, more: String*): QueryString =
    add(name, one +: more)

  /**
   * Updates parameter with given name to supplied values.
   *
   * If a parameter with given name already exists, its values are replaced
   * with the newly supplied values.
   *
   * If a parameter does not exist, it is added with supplied values.
   *
   * @param name parameter name
   * @param values parameter values
   *
   * @return new query string
   */
  def update(name: String, values: Seq[String]): QueryString

  /**
   * Updates parameter with given name to supplied values.
   *
   * If the parameter with given name already exists, its values are replaced
   * with the newly supplied values.
   *
   * If the parameter does not exist, it is added with supplied values.
   *
   * @param name parameter name
   * @param one parameter value
   * @param more additional parameter values
   *
   * @return new query string
   */
  def update(name: String, one: String, more: String*): QueryString =
    update(name, one +: more)

  /**
   * Removes parameters with given names.
   *
   * @param names parameter names
   *
   * @return new query string
   */
  def remove(names: Seq[String]): QueryString

  /**
   * Removes parameters with given names.
   *
   * @param one parameter name
   * @param more additional parameter names
   *
   * @return new query string
   */
  def remove(one: String, more: String*): QueryString =
    remove(one +: more)

  /**
   * Creates new query string by concatenating supplied query string.
   *
   * @param that query string
   *
   * @return new query string
   *
   * @note The new query string contains all values from both query strings with
   * parameter values in `that` appended to those in `this`.
   */
  def concat(that: QueryString): QueryString =
    that.isEmpty match {
      case true  => this
      case false => QueryString(toSeq ++ that.toSeq)
    }

  /**
   * Creates new query string by concatenating supplied parameters.
   *
   * @param params parameters
   *
   * @return new query string
   */
  def concat(params: Map[String, Seq[String]]): QueryString =
    concat(QueryString(params))

  /**
   * Creates new query string by concatenating supplied parameters.
   *
   * @param params parameters
   *
   * @return new query string
   */
  def concat(params: Seq[(String, String)]): QueryString =
    concat(QueryString(params))

  /**
   * Creates new query string by merging supplied query string.
   *
   * @param that query string
   *
   * @return new query string
   *
   * @note The new query string contains values from both query strings with
   * parameter values in `that` overriding those in `this`.
   */
  def merge(that: QueryString): QueryString =
    that.isEmpty match {
      case true  => this
      case false => QueryString(toMap ++ that.toMap)
    }

  /**
   * Creates new query string by merging supplied parameters.
   *
   * @param params parameters
   *
   * @return new query string
   */
  def merge(params: Map[String, Seq[String]]): QueryString =
    merge(QueryString(params))

  /**
   * Creates new query string by merging supplied parameters.
   *
   * @param params parameters
   *
   * @return new query string
   */
  def merge(params: Seq[(String, String)]): QueryString =
    merge(QueryString(params))

  /**
   * Creates new query string by selecting parameters which satisfy supplied
   * predicate.
   *
   * @param pred predicate
   *
   * @param new query string
   */
  def filter(pred: ((String, String)) => Boolean): QueryString =
    QueryString(toSeq.filter(pred))

  /**
   * Creates new query string by selecting parameters which do not satisfy
   * supplied predicate.
   *
   * @param pred predicate
   *
   * @param new query string
   */
  def filterNot(pred: ((String, String)) => Boolean): QueryString =
    QueryString(toSeq.filterNot(pred))

  /**
   * Creates new query string by selecting parameters whose names satisfy
   * supplied predicate.
   *
   * @param pred predicate
   *
   * @param new query string
   */
  def filterNames(pred: String => Boolean): QueryString =
    filter(param => pred(param._1))

  /** Gets `Seq` of name-value pairs from query string. */
  def toSeq: Seq[(String, String)]

  /**
   * Gets `Map` of query string mapping each parameter to its sequence of
   * values.
   */
  def toMap: Map[String, Seq[String]]

  /** Gets `Map` of query string mapping each parameter to its first value. */
  def toSimpleMap: Map[String, String]

  /**
   * Creates new query string by concatenating supplied query string.
   *
   * @param that query string
   *
   * @return new query string
   *
   * @note Alias to `concat`.
   */
  def ++(that: QueryString): QueryString = concat(that)

  /**
   * Creates new query string by concatenating supplied parameters.
   *
   * @param params parameters
   *
   * @return new query string
   *
   * @note Alias to `concat`.
   */
  def ++(params: Map[String, Seq[String]]): QueryString = concat(params)

  /**
   * Creates new query string by concatenating supplied parameters.
   *
   * @param params parameters
   *
   * @return new query string
   *
   * @note Alias to `concat`.
   */
  def ++(params: Seq[(String, String)]): QueryString = concat(params)

  /**
   * Creates new query string by merging supplied query string.
   *
   * @param that query string
   *
   * @return new query string
   *
   * @note Alias to `merge`.
   */
  def <<(that: QueryString): QueryString = merge(that)

  /**
   * Creates new query string by merging supplied parameters.
   *
   * @param params parameters
   *
   * @return new query string
   *
   * @note Alias to `merge`.
   */
  def <<(params: Map[String, Seq[String]]): QueryString = merge(params)

  /**
   * Creates new query string by merging supplied parameters.
   *
   * @param params parameters
   *
   * @return new query string
   *
   * @note Alias to `merge`.
   */
  def <<(params: Seq[(String, String)]): QueryString = merge(params)
}

/** Provides factory for `QueryString`. */
object QueryString {
  /** Gets empty query string. */
  def empty: QueryString = EmptyQueryString

  /**
   * Creates query string from parameters.
   *
   * @param params parameters
   */
  def apply(params: Map[String, Seq[String]]): QueryString =
    params.isEmpty match {
      case true  => EmptyQueryString
      case false => MapQueryString(params)
    }

  /**
   * Creates query string from parameters.
   *
   * @param params parameters
   */
  def apply(params: Seq[(String, String)]): QueryString =
    params.isEmpty match {
      case true  => EmptyQueryString
      case false => SeqQueryString(params)
    }

  /**
   * Creates query string from parameters.
   *
   * @param one parameter
   * @param more additional parameters
   */
  def apply(one: (String, String), more: (String, String)*): QueryString =
    apply(one +: more)

  /**
   * Creates query string from urlencoded query string.
   *
   * @param query encoded query string
   */
  def apply(query: String): QueryString =
    parse(query) match {
      case Nil    => EmptyQueryString
      case params => SeqQueryString(params)
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

  def add(name: String, values: Seq[String]) =
    SeqQueryString(values.map(value => name -> value))

  def update(name: String, values: Seq[String]) =
    SeqQueryString(values.map(value => name -> value))

  def remove(names: Seq[String]) = this

  override def concat(other: QueryString) = other
  override def merge(other: QueryString) = other
  override def filter(pred: ((String, String)) => Boolean) = this
  override def filterNames(pred: String => Boolean) = this

  override val toString = ""
}

private case class MapQueryString(toMap: Map[String, Seq[String]]) extends QueryString {
  lazy val names = toMap.keys.toSeq

  def get(name: String) = toMap.get(name).flatMap(_.headOption)
  def getValues(name: String) = toMap.get(name).getOrElse(Nil)
  def contains(name: String) = toMap.contains(name)
  def isEmpty = toMap.isEmpty

  def add(name: String, values: Seq[String]) =
    MapQueryString(toMap + { name -> (getValues(name) ++ values) })

  def update(name: String, values: Seq[String]) =
    MapQueryString(toMap + { name -> values })

  def remove(names: Seq[String]) =
    names.isEmpty match {
      case true  => this
      case false => MapQueryString(toMap.filterNot(x => names.contains(x._1)))
    }

  lazy val toSeq =
    toMap.toSeq
      .flatMap { case (name, values) => values.map(value => name -> value) }

  lazy val toSimpleMap =
    toMap.collect { case (name, Seq(value, _*)) => name -> value }
      .toMap

  override lazy val toString = QueryString.format(toMap)
}

private case class SeqQueryString(toSeq: Seq[(String, String)]) extends QueryString {
  lazy val names = toSeq.map(_._1).distinct

  def get(name: String) = toSeq.collectFirst { case (`name`, value) => value }
  def getValues(name: String) = toSeq.collect { case (`name`, value) => value }
  def contains(name: String) = toSeq.exists(_._1 == name)
  def isEmpty = toSeq.isEmpty

  def add(name: String, values: Seq[String]) =
    SeqQueryString(toSeq ++ values.map { value => name -> value })

  def update(name: String, values: Seq[String]) =
    SeqQueryString(toSeq.filterNot(_._1 == name) ++ values.map { value => name -> value })

  def remove(names: Seq[String]) =
    names.isEmpty match {
      case true  => this
      case false => SeqQueryString(toSeq.filterNot(x => names.contains(x._1)))
    }

  lazy val toMap =
    toSeq.groupBy(_._1)
      .collect { case (name, params) => name -> params.map(_._2) }
      .toMap

  lazy val toSimpleMap =
    toSeq.groupBy(_._1)
      .collect { case (name, params) => name -> params.head._2 }
      .toMap

  override lazy val toString = QueryString.format(toSeq)
}
