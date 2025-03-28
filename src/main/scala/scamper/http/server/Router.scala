/*
 * Copyright 2025 Carlos Conyers
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

import java.io.File

import scamper.http.websocket.WebSocketApplication

import RequestMethod.Registry.{ Delete, Get, Post, Put }

/**
 * Defines router for request handling.
 *
 * {{{
 * import scala.language.implicitConversions
 *
 * import scamper.http.ResponseStatus.Registry.{ BadRequest, NotFound, Ok }
 * import scamper.http.server.{ ParameterNotConvertible, ServerApplication, toServerHttpRequest }
 * import scamper.http.stringToEntity
 *
 * val app = ServerApplication()
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
 *     val id = req.pathParams.getInt("id")
 *     messages.get(id)
 *      .map(Ok(_))
 *      .getOrElse(NotFound())
 *   }
 *
 *   router.recover { req =>
 *     { case _: ParameterNotConvertible => BadRequest(req.target.toString) }
 *   }
 * }
 * }}}
 */
trait Router:
  /** Gets mount path. */
  def mountPath: String

  /**
   * Gets absolute path of supplied router path.
   *
   * @param path router path
   *
   * @throws java.lang.IllegalArgumentException if router path is not `*` and
   * does not begin with `/` or if it escapes mount path
   *
   * @note If `*` is supplied as router path, its absolute path is also `*`.
   */
  def getAbsolutePath(path: String): String =
    NormalizePath(path) match
      case ""   => mountPath
      case "/"  => mountPath
      case "*"  => "*"
      case path =>
        if !path.startsWith("/") || path.matches("/\\.\\.(/.*)?") then
          throw IllegalArgumentException(s"Invalid router path: $path")

        mountPath == "/" match
          case true  => path
          case false => mountPath + path

  /**
   * Resets router.
   *
   * @return this router
   */
  def reset(): this.type

  /**
   * Adds server lifecycle hook.
   *
   * @param hook lifecycle hook
   *
   * @return this router
   *
   * @note On startup, hooks are called in the order they are added; on
   * shutdown, they are called in reverse order.
   */
  def trigger(hook: LifecycleHook): this.type

  /**
   * Adds supplied request handler.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param handler request handler
   *
   * @return this router
   *
   * @note If request handler implements [[LifecycleHook]], it is also added as
   * a lifecycle hook.
   */
  def incoming(handler: RequestHandler): this.type

  /**
   * Adds supplied handler for specified request methods to given router path.
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
   *
   * @note If request handler implements [[LifecycleHook]], it is also added as
   * a lifecycle hook.
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
   *
   * @note If request handler implements [[LifecycleHook]], it is also added as
   * a lifecycle hook.
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
   *
   * @note If request handler implements [[LifecycleHook]], it is also added as
   * a lifecycle hook.
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
   *
   * @note If request handler implements [[LifecycleHook]], it is also added as
   * a lifecycle hook.
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
   *
   * @note If request handler implements [[LifecycleHook]], it is also added as
   * a lifecycle hook.
   */
  def delete(path: String)(handler: RequestHandler): this.type =
    incoming(path, Delete)(handler)

  /**
   * Adds supplied WebSocket application to given router path.
   *
   * @param path router path
   * @param app WebSocket application
   *
   * @return this router
   *
   * @note If WebSocket app implements [[LifecycleHook]], it is also added as a
   * lifecycle hook.
   */
  def websocket(path: String)(app: WebSocketApplication[?]): this.type =
    app match
      case hook: LifecycleHook =>
        trigger(hook)
        incoming(path, Get)(WebSocketRequestHandler(app))

      case _ =>
        incoming(path, Get)(WebSocketRequestHandler(app))

  /**
   * Mounts file server at given router path.
   *
   * At request time, the mount path is stripped from target path, and remaining
   * path is used to locate a file in source directory.
   *
   * ### File Mapping Examples
   *
   * | Mount Path  | Source Directory | Target Path               | Maps to |
   * | ----------- | ---------------- | ------------------------- | ------- |
   * | /images     | /tmp             | /images/logo.png          | /tmp/logo.png |
   * | /images     | /tmp             | /images/icons/warning.png | /tmp/icons/warning.png |
   *
   * @param path router path at which file server is mounted
   * @param source directory from which files are served
   * @param defaults default file names
   *
   * @return this router
   *
   * @note If request matches a directory, and if a file with one of the default
   * file names exists in that directory, the server sends 303 (See Other) with
   * Location header pointing to default file.
   */
  def fileserver(path: String, source: File, defaults: String*): this.type =
    mount(FileServer(source, defaults), path, None)

  /**
   * Mounts router application at given router path.
   *
   * @param path router path at which router application is mounted
   * @param app router application
   *
   * @return this router
   *
   * @note If router app implements [[LifecycleHook]], it is also added as a
   * lifecycle hook.
   */
  def route(path: String)(app: RouterApplication): this.type =
    mount(app, path, None)

  /**
   * Mounts conditional router application at given router path.
   *
   * @param path router path at which router application is mounted
   * @param predicate condition under which router is applied
   * @param app router application
   *
   * @return this router
   *
   * @note If router app implements [[LifecycleHook]], it is also added as a
   * lifecycle hook.
   */
  def route(path: String, predicate: RequestPredicate)(app: RouterApplication): this.type =
    mount(app, path, Some(predicate))

  /**
   * Adds error handler.
   *
   * The handler is appended to existing error handler chain.
   *
   * @param handler error handler
   *
   * @return this router
   *
   * @note If error handler implements [[LifecycleHook]], it is also added as a
   * lifecycle hook.
   */
  def recover(handler: ErrorHandler): this.type

  /**
   * Adds supplied response filter.
   *
   * The filter is appended to existing response filter chain.
   *
   * @param filter response filter
   *
   * @return this router
   *
   * @note If response filter implements [[LifecycleHook]], it is also added as
   * a lifecycle hook.
   */
  def outgoing(filter: ResponseFilter): this.type

  private def mount(app: RouterApplication, path: String, predicate: Option[RequestPredicate]): this.type =
    val router = StandardRouter(mountPath + MountPath.normalize(path))

    app(router)

    val hooks   = router.getLifecycleHooks()
    val handler = MountRequestHandler(
      router.mountPath,
      predicate.map { ConditionalRequestHandler(_, router.getRequestHandler()) }
        .getOrElse { router.getRequestHandler() }
    )

    app match
      case hook: LifecycleHook =>
        trigger(hook)
        hooks.foreach(trigger)
        incoming(handler)

      case _ =>
        hooks.foreach(trigger)
        incoming(handler)
