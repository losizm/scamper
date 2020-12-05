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
package scamper.server

import scala.util.Try
import scala.util.matching.Regex

import scamper.{ HttpMessage, HttpRequest, RequestMethod }
import scamper.Auxiliary.StringType
import scamper.Validate._

private class TargetedRequestHandler private (handler: RequestHandler, target: Target, methods: Seq[RequestMethod]) extends RequestHandler {
  def apply(req: HttpRequest): HttpMessage =
    target.matches(req.path) && (methods.isEmpty || methods.contains(req.method)) match {
      case true  =>
        val params = new TargetedPathParameters(target.getParams(req.path))
        handler(req.withAttribute("scamper.server.request.parameters" -> params))

      case false =>
        req
    }
}

private object TargetedRequestHandler {
  def apply(handler: RequestHandler, path: String, methods: Seq[RequestMethod]): TargetedRequestHandler =
    new TargetedRequestHandler(
      notNull(handler),
      new Target(NormalizePath(notNull(path))),
      noNulls(methods)
    )
}

private class TargetedPathParameters(params: Map[String, String]) extends PathParameters {
  def getString(name: String): String =
    params.getOrElse(name, throw ParameterNotFound(name))

  def getInt(name: String): Int = {
    val value = getString(name)
    Try(value.toInt).getOrElse(throw ParameterNotConvertible(name, value))
  }

  def getLong(name: String): Long = {
    val value = getString(name)
    Try(value.toLong).getOrElse(throw ParameterNotConvertible(name, value))
  }
}

private class Target(path: String) {
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

  def getParams(path: String): Map[String, String] =
    params.isEmpty match {
      case true  => Map.empty
      case false =>
        val segments = segmentize(path)
        params.map {
          case (name, getValue) => name -> getValue(segments)
        }.toMap
    }

  def matches(path: String): Boolean = path.matches(regex)

  private def segmentize(path: String): Seq[String] =
    path match {
      case "/" => Nil
      case "*" => Nil
      case p   => p.tail.split("/").toSeq
    }
}
