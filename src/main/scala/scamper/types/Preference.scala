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
package scamper.types

import scamper.Grammar.{ QuotableString, QuotedString, Token }
import scamper.ListParser

/**
 * Standardized type for Prefer and Preference-Applied header values.
 *
 * @see [[scamper.headers.Prefer]]
 * @see [[scamper.headers.PreferenceApplied]]
 */
trait Preference {
  /** Gets name. */
  def name: String

  /** Gets optional value. */
  def value: Option[String]

  /** Gets parameters. */
  def params: Map[String, Option[String]]

  /** Returns formatted preference. */
  override lazy val toString: String = {
    def pair(a: String, b: Option[String]) = a + {
      b.map(x => "=" + Token(x).getOrElse(s"""\"$x\"""")).getOrElse("")
    }

    pair(name, value) + params.map {
      case (name, value) => "; " + pair(name, value)
    }.mkString
  }
}

/** Provides registered preferences. */
object Preferences {
  /** Preference for `wait=duration`. */
  final case class `wait=duration`(seconds: Long) extends Preference {
    val name: String = "wait"
    val value: Option[String] = Some(seconds.toString)
    val params: Map[String, Option[String]] = Map.empty
  }

  /** Preference for `handling=strict`. */
  case object `handling=strict` extends Preference {
    val name: String = "handling"
    val value: Option[String] = Some("strict")
    val params: Map[String, Option[String]] = Map.empty
  }

  /** Preference for `handling=lenient`. */
  case object `handling=lenient` extends Preference {
    val name: String = "handling"
    val value: Option[String] = Some("lenient")
    val params: Map[String, Option[String]] = Map.empty
  }

  /** Preference for `return=representation`. */
  case object `return=representation` extends Preference {
    val name: String = "return"
    val value: Option[String] = Some("representation")
    val params: Map[String, Option[String]] = Map.empty
  }

  /** Preference for `return=minimal`. */
  case object `return=minimal` extends Preference {
    val name: String = "return"
    val value: Option[String] = Some("minimal")
    val params: Map[String, Option[String]] = Map.empty
  }

  /** Preference for `respond-async`. */
  case object `respond-async` extends Preference {
    val name: String = "respond-async"
    val value: Option[String] = None
    val params: Map[String, Option[String]] = Map.empty
  }
}
import Preferences._

/** Provides factory methods for `Preference`. */
object Preference {
  /** Parses formatted preference. */
  def parse(preference: String): Preference = {
    val pref = preference.split(";", 2)

    val (name, value) = pref.head.split("=", 2).map(_.trim) match {
      case Array(name, "")    => name -> None
      case Array(name, value) => name -> RawValue(value)
      case Array(name)        => name -> None
    }

    val params = pref.tail.flatMap(ListParser(_, true)).map(_.split("=", 2)).map {
      case Array(name, value) => name -> RawValue(value)
      case Array(name)        => name -> None
    }

    apply(name, value, params.toMap)
  }

  /** Parses formatted list of preferences. */
  def parseAll(preferences: String): Seq[Preference] =
    ListParser(preferences).map(parse)

  /** Creates Preference with supplied name. */
  def apply(name: String): Preference =
    apply(name, None, Map.empty[String, Option[String]])

  /** Creates Preference with supplied name and value. */
  def apply(name: String, value: String): Preference =
    apply(name, Some(value), Map.empty[String, Option[String]])

  /** Creates Preference with supplied name and params. */
  def apply(name: String, params: Map[String, Option[String]]): Preference =
    apply(name, None, params)

  /** Creates Preference with supplied name, value, and parameters. */
  def apply(name: String, value: String, params: Map[String, Option[String]]): Preference =
    apply(name, Some(value), params)

  /** Creates Preference with supplied name, optional value, and parameters. */
  def apply(name: String, value: Option[String], params: Map[String, Option[String]]): Preference =
    name.toLowerCase match {
      case "wait" if value.exists(_ matches "\\d+")     => `wait=duration`(value.get.toLong)
      case "return" if value.contains("representation") => `return=representation`
      case "return" if value.contains("minimal")        => `return=minimal`
      case "handling" if value.contains("strict")       => `handling=strict`
      case "handling" if value.contains("lenient")      => `handling=lenient`
      case "respond-async"                              => `respond-async`
      case name => PreferenceImpl(Name(name), value.filterNot(_.isEmpty).map(Value), Params(params))
    }

  /** Destructures Preference. */
  def unapply(preference: Preference): Option[(String, Option[String], Map[String, Option[String]])] =
    Some((preference.name, preference.value, preference.params))

  private def Name(value: String): String =
    Token(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid name: $value")
    }

  private def Value(value: String): String =
    Token(value) orElse QuotableString(value) getOrElse {
      throw new IllegalArgumentException(s"Invalid value: $value")
    }

  private def RawValue(value: String): Option[String] =
    Token(value) orElse QuotedString(value) orElse {
      throw new IllegalArgumentException(s"Invalid value: $value")
    }

  private def Params(params: Map[String, Option[String]]): Map[String, Option[String]] =
    params.map { case (name, value) => Name(name) -> value.filterNot(_.isEmpty).map(Value) }
}

private case class PreferenceImpl(name: String, value: Option[String], params: Map[String, Option[String]]) extends Preference
