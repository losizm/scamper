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

import java.net.Socket

import scamper.{ HttpMessage, HttpRequest }
import scamper.Implicits.stringToEntity
import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.{ BadRequest, SwitchingProtocols }
import scamper.headers.{ Connection, Upgrade }
import scamper.server.Implicits.ServerHttpMessageType
import scamper.types.Implicits.stringToProtocol
import scamper.websocket._

private class WebSocketRequestHandler private (handler: (WebSocketSession) => Any) extends RequestHandler {
  def apply(req: HttpRequest): HttpMessage =
    isWebSocketUpgrade(req) match {
      case true  =>
        try {
          checkWebSocketRequest(req)

          SwitchingProtocols()
            .withUpgrade("websocket")
            .withConnection("Upgrade")
            .withSecWebSocketAccept(acceptWebSocketKey(req.secWebSocketKey))
            .withAttribute("scamper.server.connection.upgrade" -> { (socket: Socket) =>
              val session = WebSocketSession.forServer(
                socket,
                req.correlate,
                req.target,
                req.secWebSocketVersion,
                Some(req.logger)
              )
              handler(session)
            })
        } catch {
          case InvalidWebSocketRequest(message) => BadRequest(message)
        }
      case false => req
    }

  private def isWebSocketUpgrade(req: HttpRequest): Boolean =
    req.method == GET &&
      req.upgrade.exists { protocol =>
        protocol.name == "websocket" && protocol.version.isEmpty
      }
}

private object WebSocketRequestHandler {
  def apply(handler: (WebSocketSession) => Any) = new WebSocketRequestHandler(handler)
}
