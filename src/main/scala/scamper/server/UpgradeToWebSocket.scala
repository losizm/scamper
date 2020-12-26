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

import java.net.Socket

import scamper.{ Entity, HttpRequest, HttpResponse }
import scamper.Implicits.stringToEntity
import scamper.ResponseStatus.Registry.{ BadRequest, SwitchingProtocols }
import scamper.headers.{ Connection, Upgrade }
import scamper.types.Implicits.stringToProtocol
import scamper.websocket._

/**
 * Upgrades request to WebSocket connection.
 *
 * {{{
 * import scamper.HttpRequest
 * import scamper.ResponseStatus.Registry.Unauthorized
 * import scamper.server.{ ServerApplication, UpgradeToWebSocket }
 *
 * val app = ServerApplication()
 *
 * app.get("/chat/:roomId") { req =>
 *   def authorize(req: HttpRequest): Boolean = ...
 *
 *   authorize(req) match {
 *     case true  =>
 *       UpgradeToWebSocket(req) { session =>
 *         // Set up session
 *         ...
 *         session.open()
 *       }
 *     case false => Unauthorized()
 *   }
 * }
 * }}}
 */
object UpgradeToWebSocket {
  /**
   * Upgrades request to WebSocket connection.
   *
   * If request is successfully upgraded to WebSocket, supplied handler will be
   * called with session after connection is established.
   *
   * @param req request
   * @param handler WebSocket session handler
   *
   * @return '''101 SwitchingProtocols''' if valid WebSocket upgrade request;
   * otherwise, '''400 Bad Request'''
   */
  def apply[T](req: HttpRequest)(handler: WebSocketSession => T): HttpResponse =
    try {
      WebSocket.validate(req)

      if (WebSocket.enablePermessageDeflate(req))
        createResponse(req, handler, DeflateMode.Message)
          .setSecWebSocketExtensions("permessage-deflate; client_no_context_takeover; server_no_context_takeover")
      else if (WebSocket.enableWebkitDeflateFrame(req))
        createResponse(req, handler, DeflateMode.Frame)
          .setSecWebSocketExtensions("x-webkit-deflate-frame; no_context_takeover")
      else
        createResponse(req, handler, DeflateMode.None)
    } catch {
      case InvalidWebSocketRequest(message) => BadRequest(message)
    }

  private def createResponse[T](req: HttpRequest, handler: WebSocketSession => T, deflateMode: DeflateMode): HttpResponse =
    SwitchingProtocols()
      .setUpgrade("websocket")
      .setConnection("Upgrade")
      .setSecWebSocketAccept(WebSocket.acceptKey(req.secWebSocketKey))
      .putAttributes("scamper.server.connection.upgrade" -> { (socket: Socket) =>
        val sessionRequest = req.setBody(Entity.empty).putAttributes("scamper.server.message.socket" -> socket)
        handler(WebSocketSession.forServer(sessionRequest, deflateMode))
      })
}
