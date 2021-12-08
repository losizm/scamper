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

import scala.collection.mutable.ListBuffer

import Validate.{ noNulls, notNull }

private class RouterImpl private (val mountPath: String) extends Router:
  private val in  = new ListBuffer[RequestHandler]
  private val out = new ListBuffer[ResponseFilter]
  private val err = new ListBuffer[ErrorHandler]
  private val svc = new ListBuffer[ManagedService]

  def reset(): this.type = synchronized {
    in.clear()
    out.clear()
    err.clear()
    svc.clear()
    this
  }

  def manage(services: Seq[ManagedService]): this.type = synchronized {
    svc ++= noNulls(services, "services")
    this
  }

  def incoming(handler: RequestHandler): this.type = synchronized {
    in += notNull(handler, "handler")
    toManagedService(handler).foreach(svc.+=)
    this
  }

  def incoming(path: String, methods: RequestMethod*)(handler: RequestHandler): this.type = synchronized {
    in += TargetRequestHandler(
      toAbsolutePath(notNull(path, "path")),
      notNull(methods, "methods"),
      notNull(handler, "handler")
    )
    toManagedService(handler).foreach(svc.+=)
    this
  }

  def outgoing(filter: ResponseFilter): this.type = synchronized {
    out += notNull(filter, "filter")
    toManagedService(filter).foreach(svc.+=)
    this
  }

  def recover(handler: ErrorHandler): this.type = synchronized {
    err += notNull(handler, "handler")
    toManagedService(handler).foreach(svc.+=)
    this
  }

  private[server] def getManagedServices(): Seq[ManagedService] =
    synchronized(svc.toSeq)

  private[server] def createRequestHandler(): RequestHandler = synchronized {
    RouterRequestHandler(
      RequestHandler.coalesce(in.toSeq),
      ResponseFilter.chain(out.toSeq),
      ErrorHandler.coalesce(err.toSeq)
    )
  }

  private def toManagedService[T](value: T): Option[ManagedService] =
    value match
      case service: ManagedService => Some(service)
      case _                       => None

  private class RouterRequestHandler(in: RequestHandler, out: ResponseFilter, err: ErrorHandler) extends RequestHandler:
    private val attributes = Seq(
      "scamper.http.server.message.server",
      "scamper.http.server.message.socket",
      "scamper.http.server.message.requestCount",
      "scamper.http.server.message.correlate",
      "scamper.http.server.message.logger"
    )

    def apply(req: HttpRequest) =
      (try in(req) catch err(req)) match
        case req: HttpRequest  => req
        case res: HttpResponse => out(addAttributes(res, req))

    private def addAttributes(res: HttpResponse, req: HttpRequest): HttpResponse =
      res.putAttributes(getAttributes(req))

    private def getAttributes(req: HttpRequest): Map[String, Any] =
      attributes.flatMap(name => req.getAttribute[Any](name).map(value => name -> value))
        .:+("scamper.http.server.response.request" -> req)
        .toMap

private object RouterImpl:
  def apply(mountPath: String): RouterImpl =
    new RouterImpl(MountPath.normalize(mountPath))
