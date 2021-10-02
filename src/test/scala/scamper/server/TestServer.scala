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
package scamper.server

import scala.collection.concurrent.TrieMap
import scala.language.implicitConversions

import scamper.*
import scamper.Implicits.given
import scamper.auth.*
import scamper.headers.*
import scamper.logging.*
import scamper.types.given

import ResponseStatus.Registry.*
import Uri.{ http, https }

trait TestServer:
  private given BodyParser[Array[Byte]] = BodyParser.bytes(8192)

  def getServer(secure: Boolean = false, logging: Boolean = false): HttpServer =
    val app = HttpServer
      .app()
      .logger(if logging then ConsoleLogger else NullLogger)
      .backlogSize(8)
      .poolSize(2)
      .queueSize(4)
      .bufferSize(1024)
      .readTimeout(500)
      .headerLimit(20)
      .incoming(doAuditLog("Incoming request"))
      .outgoing(doAuditLog("Outgoing response"))
      .get("/")(doHome)
      .get("/about")(doAbout)
      .post("/echo")(doEcho)
      .get("/throwException")(doThrowException)
      .incoming("/notImplemented")(doNotImplemented)
      .route("/api/messages")(MessageApplication)
      .route("/cookies")(CookieApplication)
      .websocket("/chat/:id")(WebSocketChatServer)
      .files("/files/riteshiff", Resources.riteshiff, "home.html")
      .recover(doError)

    if secure then
      app.secure(Resources.keystore, "letmein", "pkcs12")

    app.create("localhost", 0)

  def withServer[T](secure: Boolean)(f: HttpServer => T): T =
    val server = getServer(secure)
    try
      f(server)
    finally
      server.close()

  def serverUri(using server: HttpServer): Uri =
    server.isSecure match
      case true  => https(server.host.getHostAddress + ":" + server.port)
      case false => http (server.host.getHostAddress + ":" + server.port)

  private def doAuditLog[T <: HttpMessage](prefix: String)(msg: T): T =
    msg.logger.info {
      val eol = System.getProperty("line.separator")
      s"$prefix (correlate=${msg.correlate})" +
      eol +
      msg.headers
        .map(_.toString)
        .mkString(msg.startLine.toString + eol, eol, eol)
    }
    msg

  private def doHome(req: HttpRequest): HttpResponse =
    Ok()

  private def doAbout(req: HttpRequest): HttpResponse =
    Ok("This is a test server.")
      .setContentType("text/plain")

  private def doEcho(req: HttpRequest): HttpResponse =
    Ok(req.as[Array[Byte]])
      .setContentType("application/octet-stream")

  private def doThrowException(req: HttpRequest): HttpResponse =
    throw Exception("Something went wrong")

  private def doNotImplemented(req: HttpRequest): HttpResponse =
    ???

  private def doError(req: HttpRequest): PartialFunction[Throwable, HttpResponse] =
    case _: NotImplementedError => NotImplemented()
    case _: ReadLimitExceeded   => PayloadTooLarge()
    case _: EntityTooLarge      => PayloadTooLarge()
    case _                      => InternalServerError()
