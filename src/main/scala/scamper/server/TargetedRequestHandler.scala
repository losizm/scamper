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
package scamper.server

import java.nio.file.{ Path, Paths }

import scala.util.Try
import scala.util.matching.Regex

import scamper.{ HttpRequest, HttpResponse, RequestMethod }
import scamper.Auxiliary.StringType

private class TargetedRequestHandler private (handler: RequestHandler, targetPath: String, targetMethod: Option[RequestMethod]) extends RequestHandler {
  private val target = new Target(targetPath)

  def apply(req: HttpRequest): Either[HttpRequest, HttpResponse] =
    if (isTargeted(req.method) && isTargeted(req.path))
      handler(req.withAttribute("scamper.server.request.parameters" -> target.getParams(req.path)))
    else
      Left(req)

  private def isTargeted(method: RequestMethod): Boolean =
    targetMethod.map(targetMethod => targetMethod == method).getOrElse(true)

  private def isTargeted(path: String): Boolean =
    target.matches(path)
}

private object TargetedRequestHandler {
  def apply(handler: RequestHandler, path: String, method: Option[RequestMethod]): TargetedRequestHandler = {
    if (!path.startsWith("/"))
      throw new IllegalArgumentException(s"Invalid target path: $path")

    new TargetedRequestHandler(handler, path, method)
  }
}

private class TargetedRequestParameters(params: Map[String, String]) extends RequestParameters {
  def getString(name: String): String = params.getOrElse(name, throw ParameterNotFound(name))

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
  if (!path.matchesAny("/", "\\*", """(/:\w+|/[\w+\-.~%]+)+""", """(/:\w+|/[\w+\-.~%]+)*/\*\w+"""))
    throw new IllegalArgumentException(s"Invalid target path: $path")

  private val segments = path match {
    case "/" => Nil
    case "*" => Nil
    case _   => path.tail.split("/").toSeq
  }

  private val regex = path match {
    case "/" => "/"
    case "*" => "\\*"
    case _   => "/" + segments.map {
      case s if s.matches("(:\\w+)")   => "[^/]+"
      case s if s.matches("(\\*\\w+)") => ".*"
      case s => Regex.quote(s)
    }.mkString("/")
  }

  private val params: Map[String, (Path => String)] = segments.zipWithIndex.collect {
    case (s, i) if s.matches("(:\\w+)")   => s.tail -> { (p: Path) => p.getName(i).toString }
    case (s, i) if s.matches("(\\*\\w+)") => s.tail -> { (p: Path) => p.subpath(i, p.getNameCount).toString }
  }.toMap

  def getParams(path: String): Map[String, String] =
    if (params.isEmpty) Map.empty
    else {
      val p = Paths.get(path).normalize()
      params.map {
        case (name, getValue) => name -> getValue(p)
      }.toMap
    }

  def matches(path: String): Boolean = path.matches(regex)
}
