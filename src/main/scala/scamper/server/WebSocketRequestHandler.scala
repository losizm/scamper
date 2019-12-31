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

import java.net.Socket
import java.security.MessageDigest

import scamper.{ Base64, HeaderNotFound, HttpMessage, HttpRequest }
import scamper.Implicits.{ stringToEntity, tupleToHeader }
import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.{ BadRequest, SwitchingProtocols }
import scamper.headers.{ Connection, Upgrade }
import scamper.server.Implicits.ServerHttpMessageType
import scamper.types.Implicits.stringToProtocol
import scamper.websocket._

private class WebSocketRequestHandler private (handler: (WebSocketSession) => Any) extends RequestHandler {
  def apply(req: HttpRequest): HttpMessage =
    checkUpgrade(req) match {
      case true  =>
        (checkConnection(req), checkWebSocketKey(req), checkWebSocketVersion(req)) match {
          case (false, _, _) => BadRequest("Upgrade connection not requested")
          case (_, false, _) => BadRequest("Invalid or missing websocket key")
          case (_, _, false) => BadRequest("Invalid or missing websocket version")
          case _ =>
            SwitchingProtocols()
              .withUpgrade("websocket")
              .withConnection("Upgrade")
              .withSecWebSocketAccept(getWebSocketAcceptValue(req))
              .withAttribute("scamper.server.connection.upgrade" -> { (socket: Socket) =>
                val properties = Map(
                  "session.id" -> req.correlate,
                  "session.target" -> req.target,
                  "session.protocolVersion" -> req.secWebSocketVersion,
                  "session.logger" -> req.logger
                )
                val session = ServerWebSocketSession(socket, properties)
                handler(session)
              })
        }
      case false => req
    }

  private def checkUpgrade(req: HttpRequest): Boolean =
    req.upgrade.exists { protocol =>
      protocol.name == "websocket" && protocol.version.isEmpty
    }

  private def checkConnection(req: HttpRequest): Boolean =
    req.connection.exists(_ equalsIgnoreCase "upgrade")

  private def checkWebSocketKey(req: HttpRequest): Boolean =
    req.getSecWebSocketKey
      .map(Base64.decode)
      .map(_.size == 16)
      .getOrElse(false)

  private def checkWebSocketVersion(req: HttpRequest): Boolean =
    req.getSecWebSocketVersion
      .map(_ == "13")
      .getOrElse(false)

  private def getWebSocketAcceptValue(req: HttpRequest): String =
    Base64.encodeToString { hash(req.secWebSocketKey + guid) }

  private def hash(value: String): Array[Byte] =
    MessageDigest.getInstance("SHA-1").digest(value.getBytes("utf-8"))
}

private object WebSocketRequestHandler {
  def apply(handler: (WebSocketSession) => Any) = new WebSocketRequestHandler(handler)
}
