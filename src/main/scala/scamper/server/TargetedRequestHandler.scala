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

import scala.collection.JavaConverters.asScalaIterator
import scala.util.Try
import scala.util.matching.Regex

import scamper.{ HttpRequest, HttpResponse, RequestMethod }
import scamper.ImplicitConverters.tupleToHeader
import scamper.auxiliary.StringType

private class TargetedRequestHandler private (handler: RequestHandler, targetPath: Path, targetMethod: Option[RequestMethod]) extends RequestHandler {
  private val target = new Target(targetPath)

  def apply(req: HttpRequest): Either[HttpRequest, HttpResponse] = {
    val path = Paths.get(req.path.toUrlDecoded("utf-8")).normalize()

    if (isTargeted(req.method) && isTargeted(path))
      handler(req.withHeader("X-Scamper-Request-Parameters" -> target.getParams(path)))
    else
      Left(req)
  }

  private def isTargeted(method: RequestMethod): Boolean =
    targetMethod.map(targetMethod => targetMethod == method).getOrElse(true)

  private def isTargeted(path: Path): Boolean =
    target.matches(path)
}

private object TargetedRequestHandler {
  def apply(handler: RequestHandler, path: String, method: Option[RequestMethod]): TargetedRequestHandler =
    apply(handler, Paths.get(path), method)

  def apply(handler: RequestHandler, path: Path, method: Option[RequestMethod]): TargetedRequestHandler = {
    if (!path.startsWith("/"))
      throw new IllegalArgumentException(s"Invalid target path: $path")

    new TargetedRequestHandler(handler, path.normalize(), method)
  }
}

private class TargetedRequestParameters(params: String) extends RequestParameters {
  private lazy val toMap: Map[String, String] = params.split("&").map(_.split("=")).collect {
    case Array(name, value) => name -> value.toUrlDecoded("utf-8")
  }.toMap

  def getString(name: String): String = toMap.getOrElse(name, throw ParameterNotFound(name))

  def getInt(name: String): Int = {
    val value = getString(name)
    Try(value.toInt).getOrElse(throw ParameterNotConvertible(name, value))
  }

  def getLong(name: String): Long = {
    val value = getString(name)
    Try(value.toLong).getOrElse(throw ParameterNotConvertible(name, value))
  }
}

private class Target(path: Path) {
  if (!path.toString.matchesAny("/", """(/:\w+|/[^/:*]+)+""", """(/:\w+|/[^/:*]+)*/\*\w+"""))
    throw new IllegalArgumentException(s"Invalid target path: $path")

  private val names = asScalaIterator(path.iterator).map(_.toString).toSeq

  private val params: Map[String, (Path => String)] = names.zipWithIndex.collect {
    case (name, index) if name.matches("(:\\w+)") =>
      name.tail -> { (path: Path) => path.getName(index).toString }
    case (name, index) if name.matches("(\\*\\w+)") =>
      name.tail -> { (path: Path) => path.subpath(index, path.getNameCount).toString }
  }.toMap

  private val regex = "/" + names.map {
    case name if name.matches("(:\\w+)")   => "[^/]+"
    case name if name.matches("(\\*\\w+)") => ".+"
    case name => Regex.quote(name)
  }.mkString("/")

  def getParams(path: Path): String =
    params.map {
      case (name, getValue) => name + "=" + getValue(path).toUrlEncoded("utf-8")
    }.mkString("&")

  def matches(path: Path): Boolean = path.toString.matches(regex)
}
