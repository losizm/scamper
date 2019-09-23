/*
 * Copyright 2019 Carlos Conyers
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

/**
 * Configures routing for `ServerApplication`.
 *
 * `Router` works in much the same way as [[ServerApplication]], except it is
 * configured for request handling only, and all router paths are relative to a
 * mount path defined in the owner application.
 *
 * {{{
 * import scamper.Implicits.stringToEntity
 * import scamper.ResponseStatus.Registry.{ NotFound, Ok }
 * import scamper.server.HttpServer
 * import scamper.server.Implicits.ServerHttpRequestType
 *
 * val app = HttpServer.app()
 *
 * // Mount path of router is /api
 * app.use("/api") { router =>
 *   val messages = Map(1 -> "Hello, world!", 2 -> "Goodbye, cruel world!")
 *
 *   // Will be mapped to /api/messages/:id
 *   router.get("/messages/:id") { req =>
 *     val id = req.params.getInt("id")
 *     messages.get(id)
 *      .map(Ok(_))
 *      .getOrElse(NotFound())
 *   }
 * }
 * }}}
 *
 * @see [[ServerApplication.use]]
 */
trait Router {
  /** Gets router mount path. */
  def mountPath: String

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
   * Adds supplied handler for requests with given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def incoming(path: String)(handler: RequestHandler): this.type

  /**
   * Adds supplied handler for requests with given method and router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param method request method
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def incoming(method: RequestMethod, path: String)(handler: RequestHandler): this.type

  /**
   * Adds supplied handler for HEAD requests to given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def head(path: String)(handler: RequestHandler): this.type

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
  def get(path: String)(handler: RequestHandler): this.type

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
  def post(path: String)(handler: RequestHandler): this.type

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
  def put(path: String)(handler: RequestHandler): this.type

  /**
   * Adds supplied handler for PATCH requests to given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def patch(path: String)(handler: RequestHandler): this.type

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
  def delete(path: String)(handler: RequestHandler): this.type

  /**
   * Adds supplied handler for OPTIONS requests to given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def options(path: String)(handler: RequestHandler): this.type

  /**
   * Adds supplied handler for TRACE requests to given router path.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param path router path
   * @param handler request handler
   *
   * @return this router
   */
  def trace(path: String)(handler: RequestHandler): this.type

  /**
   * Adds request handler at mount path to serve files from given source directory.
   *
   * The mount path is stripped from the router path, and the remaining path is
   * used to locate files within source directory.
   *
   * === File Mapping Examples ===
   *
   * | Mount Path | Source Directory | Router Path               | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | /tmp             | /images/logo.png          | /tmp/logo.png |
   * | /images    | /tmp             | /images/icons/warning.png | /tmp/icons/warning.png |
   * | /images    | /tmp             | /styles/main.css          | ''Doesn't map to anything'' |
   *
   * @param mountPath router path at which directory is mounted
   * @param sourceDirectory source directory from which files are served
   *
   * @return this router
   */
  def files(mountPath: String, sourceDirectory: File): this.type

  /**
   * Adds request handler at mount path to serve resources from given source
   * directory.
   *
   * The mount path is stripped from the router path, and the remaining path is
   * used to locate resources within the source directory.
   *
   * === Resource Mapping Examples ===
   *
   * | Mount Path | Source Directory | Request Path              | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | assets           | /images/logo.png          | assets/logo.png |
   * | /images    | assets           | /images/icons/warning.png | assets/icons/warning.png |
   * | /images    | assets           | /styles/main.css          | ''Doesn't map to anything'' |
   *
   * @param mountPath router path at which directory is mounted
   * @param sourceDirectory source directory from which resources are served
   *
   * @return this router
   *
   * @note The current thread's context class loader is used to load resources.
   */
  def resources(mountPath: String, sourceDirectory: String): this.type =
    resources(mountPath, sourceDirectory, Thread.currentThread.getContextClassLoader)

  /**
   * Adds request handler at mount path to serve resources from given source
   * directory.
   *
   * The mount path is stripped from the router path, and the remaining path is
   * used to locate resources within the source directory.
   *
   * === Resource Mapping Examples ===
   *
   * | Mount Path | Source Directory | Request Path              | Maps to |
   * | ---------- | ---------------- | ------------------------- | ------- |
   * | /images    | assets           | /images/logo.png          | assets/logo.png |
   * | /images    | assets           | /images/icons/warning.png | assets/icons/warning.png |
   * | /images    | assets           | /styles/main.css          | ''Doesn't map to anything'' |
   *
   * @param mountPath router path at which directory is mounted
   * @param sourceDirectory source directory from which resources are served
   * @param classLoader class loader from which resources are loaded
   *
   * @return this router
   */
  def resources(mountPath: String, sourceDirectory: String, classLoader: ClassLoader): this.type
}

