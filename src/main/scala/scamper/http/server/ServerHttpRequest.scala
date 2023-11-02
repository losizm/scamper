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
package scamper
package http
package server

import scala.language.implicitConversions

import scamper.http.headers.{ Accept, Expect }
import scamper.http.types.{ MediaRange, MediaType }

import ResponseStatus.Registry.Continue

/** Adds server extensions to `HttpRequest`. */
given toServerHttpRequest: Conversion[HttpRequest, ServerHttpRequest] = ServerHttpRequest(_)

/** Adds server extensions to `HttpRequest`. */
class ServerHttpRequest(request: HttpRequest) extends AnyVal:
  /** Gets path parameters. */
  def pathParams: PathParameters =
    request.getAttributeOrElse("scamper.http.server.request.pathParams", MapPathParameters(Map.empty))

  /**
   * Sends interim 100 (Continue) response if request includes Expect header
   * set to 100-Continue.
   *
   * @return `true` if response was sent; `false` otherwise
   */
  def continue(): Boolean =
    request.expectOption
      .collect { case value if value.equalsIgnoreCase("100-continue") => request.socket }
      .map { socket =>
        socket.writeLine(StatusLine(Continue).toString)
        socket.writeLine()
        socket.flush()
      }.isDefined

  /**
   * Finds accepted media type among supplied media types.
   *
   * The matching media type with the highest weight is returned. If multiple
   * matches are found with equal weight, the first match is returned.
   */
  def findAccepted(types: Seq[MediaType]): Option[MediaType] =
    val ranges = request.accept match
      case Nil    => Seq(MediaRange("*/*"))
      case accept => accept.sortBy(_.weight * -1)

    types.flatMap { t => ranges.find(_.matches(t)).map(_.weight -> t) }
      .sortBy(_._1 * -1)
      .headOption
      .map(_._2)
