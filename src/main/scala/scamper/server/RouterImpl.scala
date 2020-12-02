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

import java.io.File

import scala.util.Try

import scamper.Auxiliary.StringType
import scamper.RequestMethod
import scamper.RequestMethod.Registry._
import scamper.websocket.WebSocketSession

private class RouterImpl(app: ServerApplication, rawMountPath: String) extends Router {
  val mountPath = normalize(rawMountPath, true)

  def incoming(handler: RequestHandler): this.type =
    applyIncoming("*", Nil, handler)

  def incoming(path: String)(handler: RequestHandler): this.type =
    applyIncoming(path, Nil, handler)

  def incoming(path: String, methods: RequestMethod*)(handler: RequestHandler): this.type =
    applyIncoming(path, methods, handler)

  def get(path: String)(handler: RequestHandler): this.type =
    applyIncoming(path, Seq(Get), handler)

  def post(path: String)(handler: RequestHandler): this.type =
    applyIncoming(path, Seq(Post), handler)

  def put(path: String)(handler: RequestHandler): this.type =
    applyIncoming(path, Seq(Put), handler)

  def delete(path: String)(handler: RequestHandler): this.type =
    applyIncoming(path, Seq(Delete), handler)

  def files(path: String, sourceDirectory: File): this.type = synchronized {
    app.files(mountPath + normalize(path), sourceDirectory)
    this
  }

  def resources(path: String, sourceDirectory: String, classLoader: ClassLoader): this.type = synchronized {
    app.resources(mountPath + normalize(path), sourceDirectory, classLoader)
    this
  }

  def websocket[T](path: String)(handler: WebSocketSession => T): this.type = synchronized {
    normalize(path) match {
      case "*" =>
        app.websocket(mountPath)(handler)
        app.websocket(mountPath + "/*")(handler)
      case path =>
        app.websocket(mountPath + path)(handler)
    }
    this
  }

  private def applyIncoming(path: String, methods: Seq[RequestMethod], handler: RequestHandler): this.type = synchronized {
    (path == "*") match {
      case true =>
        app.incoming(mountPath, methods : _*) { handler }
        app.incoming(mountPath + "/*", methods : _*) { handler }
      case false =>
        app.incoming(mountPath + normalize(path), methods : _*) { handler }
    }
    this
  }

  private def normalize(path: String, isMountPath: Boolean = false): String =
    NormalizePath(path) match {
      case "/" => if (isMountPath) "/" else ""
      case path if path.matches("/\\.\\.(/.*)?") => throw new IllegalArgumentException(s"Invalid path: $path")
      case path if path.startsWith("/") => path
      case path => throw new IllegalArgumentException(s"Invalid path: $path")
    }
}
