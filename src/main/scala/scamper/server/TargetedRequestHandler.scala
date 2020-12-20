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

import scamper.{ HttpMessage, HttpRequest, RequestMethod }
import scamper.Validate._

private class TargetedRequestHandler(handler: RequestHandler, target: Target, methods: Seq[RequestMethod]) extends RequestHandler {
  notNull(handler)
  notNull(target)
  noNulls(methods)

  def this(handler: RequestHandler, path: String, methods: Seq[RequestMethod]) =
    this(handler, new Target(path), methods)

  def apply(req: HttpRequest): HttpMessage =
    target.matches(req.path) && (methods.isEmpty || methods.contains(req.method)) match {
      case true  =>
        handler(req.withAttribute("scamper.server.request.parameters" -> target.getParams(req.path)))

      case false =>
        req
    }
}
