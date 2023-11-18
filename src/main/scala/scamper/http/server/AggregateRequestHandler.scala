/*
 * Copyright 2023 Carlos Conyers
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

private class AggregateRequestHandler(in: RequestHandler, out: ResponseFilter, err: ErrorHandler) extends RequestHandler:
  notNull(in, "in")
  notNull(out, "out")
  notNull(err, "err")

  def apply(req: HttpRequest) =
    (try in(req) catch err(req)) match
      case req: HttpRequest  => req
      case res: HttpResponse => out(addAttributes(res, req))

  private val attributes = Seq(
    "scamper.http.server.message.server",
    "scamper.http.server.message.socket",
    "scamper.http.server.message.requestCount",
    "scamper.http.server.message.correlate"
  )

  private def addAttributes(res: HttpResponse, req: HttpRequest): HttpResponse =
    res.putAttributes(getAttributes(req))

  private def getAttributes(req: HttpRequest): Map[String, Any] =
    attributes.flatMap(name => req.getAttribute[Any](name).map(value => name -> value))
      .appended("scamper.http.server.response.request" -> req)
      .toMap
