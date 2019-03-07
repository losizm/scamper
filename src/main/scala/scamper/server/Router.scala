/*
 * Copyright 2018 Carlos Conyers
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
 * configured for request handling only. All router paths are relative to a
 * base path defined in the owner application.
 *
 * {{{
 * import scamper.ImplicitConverters.stringToEntity
 * import scamper.ResponseStatuses.{ NotFound, Ok }
 * import scamper.server.HttpServer
 * import scamper.server.Implicits.ServerHttpRequestType
 *
 * val app = HttpServer.app()
 *
 * // Base path of router is /api
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
  /**
   * Adds supplied request handler.
   *
   * The handler is appended to existing request handler chain.
   *
   * @param handler request handler
   *
   * @return this router
   */
  def request(handler: RequestHandler): this.type

  /**
   * Adds supplied request filter.
   *
   * The filter is appended to existing request handler chain.
   *
   * @param filter request filter
   *
   * @return this router
   */
  def request(filter: RequestFilter): this.type

  /**
   * Adds supplied request processor.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param processor request processor
   *
   * @return this router
   */
  def request(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for requests with given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def request(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for requests with given method and router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param method request method
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def request(method: RequestMethod, path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for HEAD requests to given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def head(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for GET requests to given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def get(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for POST requests to given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def post(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for PUT requests to given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def put(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for PATCH requests to given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def patch(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for DELETE requests to given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def delete(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for OPTIONS requests to given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def options(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds supplied processor for TRACE requests to given router path.
   *
   * The processor is appended to existing request handler chain.
   *
   * @param path router path
   * @param processor request processor
   *
   * @return this router
   */
  def trace(path: String)(processor: RequestProcessor): this.type

  /**
   * Adds request handler to serve static files from given base directory.
   *
   * Files are mapped from base directory to router path excluding path
   * prefix.
   *
   * === File Mapping Examples ===
   *
   * | Path Prefix | Base Directory | Router Path               | Maps to |
   * | ----------- | -------------- | ------------------------- | ------- |
   * | /images     | /tmp           | /images/logo.png          | /tmp/logo.png |
   * | /images     | /tmp           | /images/icons/warning.png | /tmp/icons/warning.png |
   * | /images     | /tmp           | /styles/main.css          | <em>Doesn't map to anything</em> |
   *
   * @param pathPrefix request path prefix
   * @param baseDirectory base directory from which files are served
   *
   * @return this router
   */
  def files(pathPrefix: String, baseDirectory: File): this.type

  /**
   * Adds request handler to serve static resources from given base name.
   *
   * Resources are mapped from base name to router path excluding path prefix.
   *
   * <strong>Note:</strong> If `loader` is not supplied, then the current
   * thread's context class loader is used.
   *
   * === Resource Mapping Examples ===
   *
   * | Path Prefix | Base Name | Router Path               | Maps to |
   * | ----------- | --------- | ------------------------- | ------- |
   * | /images     | assets    | /images/logo.png          | assets/logo.png |
   * | /images     | assets    | /images/icons/warning.png | assets/icons/warning.png |
   * | /images     | assets    | /styles/main.css          | <em>Doesn't map to anything</em> |
   *
   * @param pathPrefix request path prefix
   * @param baseName base name from which resources are served
   * @param loader class loader from which resources are loaded
   *
   * @return this router
   */
  def resources(pathPrefix: String, baseName: String, loader: Option[ClassLoader] = None): this.type
}

