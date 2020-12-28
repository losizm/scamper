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

import scala.collection.concurrent.TrieMap

import scamper._
import scamper.Implicits._
import scamper.auth._
import scamper.headers._
import scamper.logging._
import scamper.server.Implicits._
import scamper.types.Implicits._

import ResponseStatus.Registry._
import Uri.{ http, https }

trait TestServer {
  private implicit val bodyParser = BodyParser.bytes(8192)

  def withServer[T](f: HttpServer => T): T = {
    val server = buildServer()

    try
      f(server)
    finally
      server.close()
  }

  def serverUri(implicit server: HttpServer): Uri =
    server.isSecure match {
      case true  => https(server.host.getHostAddress + ":" + server.port)
      case false => http (server.host.getHostAddress + ":" + server.port)
    }

  private def buildServer(): HttpServer =
    HttpServer
      .app()
      .logger(NullLogger)
      .backlogSize(8)
      .poolSize(2)
      .queueSize(4)
      .bufferSize(1024)
      .readTimeout(1000)
      .headerLimit(20)
      .get("/")(doHome)
      .get("/about")(doAbout)
      .post("/echo")(doEcho)
      .route("/api/messages")(MessageApplication)
      .create("localhost", 0)

  private def doHome(req: HttpRequest): HttpResponse =
    Ok()

  private def doAbout(req: HttpRequest): HttpResponse =
    Ok("This is a test server.")
      .setContentType("text/plain")

  private def doEcho(req: HttpRequest): HttpResponse =
    Ok(req.as[Array[Byte]])
      .setContentType("application/octet-stream")
}
