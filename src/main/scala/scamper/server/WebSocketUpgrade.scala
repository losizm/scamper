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

import java.net.Socket

import scala.language.implicitConversions

import scamper.{ Entity, HttpRequest, HttpResponse }
import scamper.Implicits.stringToEntity
import scamper.ResponseStatus.Registry.{ BadRequest, SwitchingProtocols }
import scamper.headers.{ Connection, Upgrade }
import scamper.types.stringToProtocol
import scamper.websocket.{ *, given }

import Implicits.*

/**
 * Provides factory for upgrading request to WebSocket connection.
 *
 * {{{
 * import scamper.HttpRequest
 * import scamper.ResponseStatus.Registry.Unauthorized
 * import scamper.server.{ ServerApplication, WebSocketUpgrade }
 *
 * val app = ServerApplication()
 *
 * app.get("/chat/:roomId") { req =>
 *   def authorize(req: HttpRequest): Boolean = ...
 *
 *   authorize(req) match
 *     case true  =>
 *       WebSocketUpgrade(req) { session =>
 *         // Set up session
 *         ...
 *         session.open()
 *       }
 *     case false => Unauthorized()
 * }
 * }}}
 */
object WebSocketUpgrade:
  /**
   * Upgrades request to WebSocket connection.
   *
   * If request is successfully upgraded to WebSocket, the supplied application
   * will be invoked after connection is established.
   *
   * @param req request
   * @param application WebSocket application
   *
   * @return 101 (Switching Protocols) if valid WebSocket upgrade request;
   * otherwise, 400 (Bad Request)
   */
  def apply(req: HttpRequest)(application: WebSocketApplication[?]): HttpResponse =
    try
      WebSocket.validate(req)

      if WebSocket.enablePermessageDeflate(req) then
        createResponse(req, application, DeflateMode.Message)
          .setSecWebSocketExtensions("permessage-deflate; client_no_context_takeover; server_no_context_takeover")
      else if WebSocket.enableWebkitDeflateFrame(req) then
        createResponse(req, application, DeflateMode.Frame)
          .setSecWebSocketExtensions("x-webkit-deflate-frame; no_context_takeover")
      else
        createResponse(req, application, DeflateMode.None)
    catch
      case InvalidWebSocketRequest(message) => BadRequest(message)

  private def createResponse(req: HttpRequest, application: WebSocketApplication[?], deflateMode: DeflateMode): HttpResponse =
    SwitchingProtocols()
      .setUpgrade("websocket")
      .setConnection("Upgrade")
      .setSecWebSocketAccept(WebSocket.acceptKey(req.secWebSocketKey))
      .putAttributes("scamper.server.connection.upgrade" -> { (socket: Socket) =>
        val session = WebSocketSession.forServer(
          socket  = socket,
          id      = req.correlate,
          target  = req.target,
          version = req.secWebSocketVersion,
          deflate = deflateMode,
          logger  = req.logger
        )

        application(session)
      })
