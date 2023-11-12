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

import Values.*

private class TargetRequestHandler private (path: TargetPath, methods: Seq[RequestMethod], handler: RequestHandler) extends RequestHandler:
  def this(path: String, methods: Seq[RequestMethod], handler: RequestHandler) =
    this(TargetPath(path), noNulls(methods), notNull(handler))

  def apply(req: HttpRequest): HttpMessage =
    path.matches(req.path) && (methods.isEmpty || methods.contains(req.method)) match
      case true  => handler(req.putAttributes("scamper.http.server.request.pathParams" -> path.getPathParams(req.path)))
      case false => req
