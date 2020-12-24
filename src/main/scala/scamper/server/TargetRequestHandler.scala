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

import scamper.{ HttpMessage, HttpRequest, RequestMethod }
import scamper.Validate._

private class TargetRequestHandler private (path: TargetPath, methods: Seq[RequestMethod], handler: RequestHandler) extends RequestHandler {
  def apply(req: HttpRequest): HttpMessage =
    check(req) match {
      case true  => handler(req.putAttributes("scamper.server.request.parameters" -> path.getParams(req.path)))
      case false => req
    }

  @inline
  private def check(req: HttpRequest): Boolean =
    path.matches(req.path) && (methods.isEmpty || methods.contains(req.method))
}

private object TargetRequestHandler {
  def apply(path: String, methods: Seq[RequestMethod], handler: RequestHandler): TargetRequestHandler =
    new TargetRequestHandler(TargetPath(path), noNulls(methods), notNull(handler))
}
