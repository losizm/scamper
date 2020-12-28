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

import scala.collection.mutable.ListBuffer

import scamper.RequestMethod

private class RouterImpl private (val mountPath: String) extends Router {
  private val handlers = new ListBuffer[RequestHandler]

  def incoming(handler: RequestHandler): this.type = synchronized {
    handlers += handler
    this
  }

  def incoming(path: String, methods: RequestMethod*)(handler: RequestHandler): this.type = synchronized {
    handlers += TargetRequestHandler(toAbsolutePath(path), methods, handler)
    this
  }

  private[server] def createRequestHandler(): RequestHandler = synchronized {
    RequestHandler.coalesce(handlers.toSeq)
  }
}

private object RouterImpl {
  def apply(mountPath: String): RouterImpl =
    new RouterImpl(MountPath.normalize(mountPath))
}
