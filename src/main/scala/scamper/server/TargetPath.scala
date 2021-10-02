/*
 * Copyright 2021 Carlos Conyers
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
package scamper.server

import scala.util.matching.Regex

import scamper.Auxiliary.StringType

private class TargetPath private (val value: String):
  private val segments = segmentize(value)

  private val regex = value match
    case "/" => "/"
    case "*" => ".*"
    case _   => "/" + segments.map {
      case s if s.matches("(:\\w+)")   => TargetPath.one
      case s if s.matches("(\\*\\w*)") => TargetPath.rest
      case s => Regex.quote(s)
    }.mkString("/")

  private val params = segments.zipWithIndex.collect {
    case (s, i) if s.matches("(:\\w+)")   => s.tail -> { (xs: Seq[String]) => xs(i) }
    case (s, i) if s.matches("(\\*\\w+)") => s.tail -> { (xs: Seq[String]) => xs.drop(i).mkString("/") }
  }

  def getParams(path: String): PathParameters =
    MapPathParameters(params.isEmpty match
      case true  => Map.empty
      case false =>
        val segments = segmentize(path)
        params.map {
          case (name, getValue) => name -> getValue(segments)
        }.toMap
    )

  def matches(path: String): Boolean =
    path.matches(regex)

  private def segmentize(path: String): Seq[String] =
    path match
      case "/" => Nil
      case "*" => Nil
      case p   => p.tail.split("/").toSeq

private object TargetPath:
  private[TargetPath] val one  = """[\w!$%&'()+,\-.:;=@_~]+"""
  private[TargetPath] val rest = """[\w!$%&'()+,\-./:;=@_~]*"""

  def apply(value: String): TargetPath =
    new TargetPath(normalize(value))

  def normalize(value: String): String =
    val path = NormalizePath(value)

    if !path.matchesAny("/", "\\*", s"""(/:\\w+|/$one)+""", s"""(/:\\w+|/$one)*/\\*\\w*""") then
      throw IllegalArgumentException(s"Invalid target path: $path")

    path
