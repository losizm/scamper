/*
 * Copyright 2020 Carlos Conyers
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

private class Target(rawPath: String) {
  private val path = NormalizePath(rawPath)

  if (!path.matchesAny("/", "\\*", """(/:\w+|/[\w+\-.~%]+)+""", """(/:\w+|/[\w+\-.~%]+)*/\*\w*"""))
    throw new IllegalArgumentException(s"Invalid target path: $path")

  private val segments = segmentize(path)

  private val regex = path match {
    case "/" => "/"
    case "*" => ".*"
    case _   => "/" + segments.map {
      case s if s.matches("(:\\w+)")   => """[\w+\-.~%]+"""
      case s if s.matches("(\\*\\w*)") => """[\w+\-.~%/]*"""
      case s => Regex.quote(s)
    }.mkString("/")
  }

  private val params = segments.zipWithIndex.collect {
    case (s, i) if s.matches("(:\\w+)")   => s.tail -> { (xs: Seq[String]) => xs(i) }
    case (s, i) if s.matches("(\\*\\w+)") => s.tail -> { (xs: Seq[String]) => xs.drop(i).mkString("/") }
  }

  def getParams(path: String): PathParameters =
    new MapPathParameters(params.isEmpty match {
      case true  => Map.empty
      case false =>
        val segments = segmentize(path)
        params.map {
          case (name, getValue) => name -> getValue(segments)
        }.toMap
    })

  def matches(path: String): Boolean =
    path.matches(regex)

  private def segmentize(path: String): Seq[String] =
    path match {
      case "/" => Nil
      case "*" => Nil
      case p   => p.tail.split("/").toSeq
    }
}
