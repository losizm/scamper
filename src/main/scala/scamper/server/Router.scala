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

import java.io.File

import scamper.RequestMethod
import scamper.RequestMethod.Registry.{ Delete, Get, Post, Put }
import scamper.websocket.WebSocketSession

/**
 * Used for routing request handlers.
 *
 * {{{
 * import scamper.Implicits.stringToEntity
 * import scamper.ResponseStatus.Registry.{ NotFound, Ok }
 * import scamper.server.HttpServer
 * import scamper.server.Implicits.ServerHttpRequestType
 *
 * val app = HttpServer.app()
 *
 * // Mount router to /api
 * app.route("/api") { router =>
 *   val messages = Map(1 -> "Hello, world!", 2 -> "Goodbye, cruel world!")
 *
 *   // Map handler to /api/messages
 *   router.get("/messages") { req =>
 *     Ok(messages.mkString("\r\n"))
 *   }
 *
 *   // Map handler to /api/messages/:id
 *   router.get("/messages/:id") { req =>
 *     val id = req.params.getInt("id")
 *     messages.get(id)
 *      .map(Ok(_))
 *      .getOrElse(NotFound())
 *   }
 * }
 * }}}
 *
 * @see [[ServerApplication.route]]
 */
trait Router {
  /** Gets mount path. */
  def mountPath: String


  /**
   * Expands supplied router path to its absolute path.
   *
   * @param path router path
   *
   * @throws java.lang.IllegalArgumentException if router path is not `*` and
   * does not begin with `/` or if it escapes mount path
   *
   * @note If `*` is supplied as router path, its absolute path is also `*`.
   */
  def toAbsolutePath(path: String): String =
    NormalizePath(path) match {
      case ""   => mountPath
      case "*"  => "*"
      case "/"  => mountPath
      case path =>
        if (!path.startsWith("/") || path.matches("/\\.\\.(/.*)?"))
          throw new IllegalArgumentException(s"Invalid router path: $path")

        mountPath == "/" match {
          case true  => path
          case false => mountPath + path
        }
    }

  /**
   * Adds supplied request handler.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param handler request handler
   *
   * @return this router
   */
  def incoming(handler: RequestHandler): this.type

  /**
   * Adds supplied handler for requests with given router path and any of
   * specified request methods.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param methods request methods
   * @param handler request handler
   *
   * @return this router
   *
   * @note If no request methods are specified, then matches are limited to path
   * only.
   */
  def incoming(path: String, methods: RequestMethod*)(handler: RequestHandler): this.type

  /**
   * Adds supplied handler for GET requests to given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def get(path: String)(handler: RequestHandler): this.type =
    incoming(path, Get)(handler)

  /**
   * Adds supplied handler for POST requests to given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def post(path: String)(handler: RequestHandler): this.type =
    incoming(path, Post)(handler)

  /**
   * Adds supplied handler for PUT requests to given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def put(path: String)(handler: RequestHandler): this.type =
    incoming(path, Put)(handler)

  /**
   * Adds supplied handler for DELETE requests to given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def delete(path: String)(handler: RequestHandler): this.type =
    incoming(path, Delete)(handler)

  /**
   * Mounts file server at given path.
   *
   * At request time, the mount path is stripped from the router path, and the
   * remaining path is used to locate a file in the source directory or one of
   * its subdirectories.
   *
   * === File Mapping Examples ===
   *
   * | Mount Path | Source Directory | Router Path               | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | /tmp             | /images/logo.png          | /tmp/logo.png |
   * | /images    | /tmp             | /images/icons/warning.png | /tmp/icons/warning.png |
   *
   * @param path router path at which directory is mounted
   * @param source base directory from which files are served
   *
   * @return this router
   */
  def files(path: String, source: File): this.type =
    incoming(StaticFileServer(mountPath + MountPath.normalize(path), source))

  /**
   * Mounts file server (for resources) at given path.
   *
   * At request time, the mount path is stripped from the router path, and the
   * remaining path is used to locate a resource in the source directory or one
   * of its subdirectories.
   *
   * === Resource Mapping Examples ===
   *
   * | Mount Path | Source Directory | Router Path               | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | assets           | /images/logo.png          | assets/logo.png |
   * | /images    | assets           | /images/icons/warning.png | assets/icons/warning.png |
   *
   * @param path router path at which directory is mounted
   * @param source base directory from which resources are served
   *
   * @return this router
   *
   * @note The current thread's context class loader is used to load resources.
   */
  def resources(path: String, source: String): this.type =
    resources(path, source, Thread.currentThread.getContextClassLoader)

  /**
   * Mounts file server (for resources) at given path.
   *
   * At request time, the mount path is stripped from the router path, and the
   * remaining path is used to locate a resource in the source directory or one
   * of its subdirectories.
   *
   * === Resource Mapping Examples ===
   *
   * | Mount Path | Source Directory | Router Path               | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | assets           | /images/logo.png          | assets/logo.png |
   * | /images    | assets           | /images/icons/warning.png | assets/icons/warning.png |
   *
   * @param path router path at which directory is mounted
   * @param source base directory from which resources are served
   * @param loader class loader with which resources are loaded
   *
   * @return this router
   */
  def resources(path: String, source: String, loader: ClassLoader): this.type =
    incoming(StaticResourceServer(mountPath + MountPath.normalize(path), source, loader))

  /**
   * Adds WebSocket server at given router path using supplied session handler for each
   * connection.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path WebSocket path
   * @param handler WebSocket session handler
   *
   * @return this router
   */
  def websocket[T](path: String)(handler: WebSocketSession => T): this.type =
    incoming(path, Get)(WebSocketRequestHandler(handler))

  /**
   * Adds routing application at given path.
   *
   * @param path router path at which application is mounted
   * @param routing routing application
   *
   * @return this router
   */
  def route[T](path: String)(routing: RoutingApplication): this.type = {
    val router = RouterImpl(mountPath + MountPath.normalize(path))
    routing(router)
    incoming(MountRequestHandler(router.mountPath, router.createRequestHandler()))
  }
}
