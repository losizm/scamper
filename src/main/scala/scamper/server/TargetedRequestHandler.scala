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

import scamper.{ HttpRequest, HttpResponse, RequestMethod }
import scamper.auxiliary.StringType

private class TargetedRequestHandler private (handler: RequestHandler, _path: Path, exact: Boolean, _method: Option[RequestMethod]) extends RequestHandler {
  def apply(req: HttpRequest): Either[HttpRequest, HttpResponse] =
    if (isTargeted(req))
      handler(req)
    else
      Left(req)

  private def isTargeted(req: HttpRequest): Boolean =
    isTargeted(req.method) && isTargeted(req.path.toURLDecoded("utf-8"))

  private def isTargeted(method: RequestMethod): Boolean =
    _method.map(_method => _method == method).getOrElse(true)

  private def isTargeted(path: String): Boolean =
    if (exact)
      _path == Paths.get(path).normalize()
    else
      Paths.get(path).normalize().startsWith(_path)
}

private object TargetedRequestHandler {
  def apply(handler: RequestHandler, path: String, exact: Boolean, method: Option[RequestMethod]): TargetedRequestHandler =
    apply(handler, Paths.get(path), exact, method)

  def apply(handler: RequestHandler, path: Path, exact: Boolean, method: Option[RequestMethod]): TargetedRequestHandler = {
    if (!path.startsWith("/"))
      throw new IllegalArgumentException(s"Invalid target path: $path")

    new TargetedRequestHandler(handler, path.normalize(), exact, method)
  }
}
