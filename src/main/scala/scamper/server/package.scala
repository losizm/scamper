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
package scamper

/**
 * Provides HTTP server implementation.
 *
 * === Building HTTP Server ===
 *
 * To build a server, you begin with `ServerApplication`. This is a mutable
 * structure to which you apply changes to configure the server. Once the desired
 * settings are applied, you invoke one of several methods to create the server.
 *
 * {{{
 * import java.io.File
 * import scamper.BodyParser
 * import scamper.Implicits.stringToEntity
 * import scamper.ResponseStatus.Registry.{ NotFound, Ok }
 * import scamper.server.HttpServer
 * import scamper.server.Implicits._
 *
 * // Get server application
 * val app = HttpServer.app()
 *
 * // Add request handler to log all requests
 * app.incoming { req =>
 *   println(req.startLine)
 *   req
 * }
 *
 * // Add request handler to specific request method and path
 * app.get("/about") { req =>
 *   Ok("This server is powered by Scamper.")
 * }
 *
 * // Add request handler using path parameter
 * app.put("/data/:id") { req =>
 *   def update(id: Int, data: String): Boolean = ???
 *
 *   implicit val parser = BodyParser.text()
 *
 *   // Get path parameter
 *   val id = req.params.getInt("id")
 *
 *   update(id, req.as[String]) match {
 *     case true  => Ok()
 *     case false => NotFound()
 *   }
 * }
 *
 * // Serve static files
 * app.files("/main", new File("/path/to/public"))
 *
 * // Gzip response body if not empty
 * app.outgoing { res =>
 *   res.body.isKnownEmpty match {
 *     case true  => res
 *     case false => res.setGzipContentEncoding()
 *   }
 * }
 *
 * // Create server
 * val server = app.create(8080)
 *
 * printf("Host: %s%n", server.host)
 * printf("Port: %d%n", server.port)
 *
 * // Run server for 60 seconds
 * Thread.sleep(60 * 1000)
 *
 * // Close server when done
 * server.close()
 * }}}
 */
package object server {
  /**
   * Indicates response was aborted.
   *
   * A `RequestHandler` throws `ResponseAborted` if no response should be sent
   * for the request.
   */
  case class ResponseAborted(message: String) extends HttpException(message)

  /** Indicates parameter is not found. */
  case class ParameterNotFound(name: String) extends HttpException(name)

  /** Indicates parameter cannot be converted. */
  case class ParameterNotConvertible(name: String, value: String) extends HttpException(s"$name=$value")
}
