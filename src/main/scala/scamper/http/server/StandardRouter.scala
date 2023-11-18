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

import scala.collection.mutable.ListBuffer

private class StandardRouter(rawMountPath: String) extends Router:
  private val incomings = new ListBuffer[RequestHandler]
  private val outgoings = new ListBuffer[ResponseFilter]
  private val recovers  = new ListBuffer[ErrorHandler]
  private val triggers  = new ListBuffer[LifecycleHook]

  val mountPath = MountPath.normalize(rawMountPath)

  def reset(): this.type = synchronized {
    incomings.clear()
    outgoings.clear()
    recovers.clear()
    triggers.clear()
    this
  }

  def trigger(hook: LifecycleHook): this.type = synchronized {
    triggers += notNull(hook, "hook")
    this
  }

  def incoming(handler: RequestHandler): this.type = synchronized {
    incomings += notNull(handler, "handler")
    toLifecycleHook(handler).foreach(triggers.+=)
    this
  }

  def incoming(path: String, methods: RequestMethod*)(handler: RequestHandler): this.type = synchronized {
    incomings += TargetRequestHandler(
      toAbsolutePath(notNull(path, "path")),
      notNull(methods, "methods"),
      notNull(handler, "handler")
    )
    toLifecycleHook(handler).foreach(triggers.+=)
    this
  }

  def outgoing(filter: ResponseFilter): this.type = synchronized {
    outgoings += notNull(filter, "filter")
    toLifecycleHook(filter).foreach(triggers.+=)
    this
  }

  def recover(handler: ErrorHandler): this.type = synchronized {
    recovers += notNull(handler, "handler")
    toLifecycleHook(handler).foreach(triggers.+=)
    this
  }

  private def toLifecycleHook[T](value: T): Option[LifecycleHook] =
    value match
      case hook: LifecycleHook => Some(hook)
      case _                   => None

  private[server] def getLifecycleHooks(): Seq[LifecycleHook] =
    synchronized(triggers.toSeq)

  private[server] def getRequestHandler(): RequestHandler = synchronized {
    AggregateRequestHandler(
      RequestHandler.coalesce(incomings.toSeq),
      ResponseFilter.chain(outgoings.toSeq),
      ErrorHandler.coalesce(recovers.toSeq)
    )
  }
