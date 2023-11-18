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
package scamper
package http
package server

import scala.language.implicitConversions
import scala.util.Try

import scamper.http.websocket.{ StatusCode, WebSocket, WebSocketApplication }

private class WebSocketRequestHandler(app: WebSocketApplication[?]) extends RequestHandler:
  notNull(app)

  private lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  def apply(req: HttpRequest): HttpMessage =
    WebSocket.isUpgrade(req) match
      case true =>
        WebSocketUpgrade(req) { session =>
          try
            app(session)
          catch case err: Exception =>
            logger.error(s"Error encountered in WebSocket session ${session.id} (correlate=${req.correlate})", err)
            Try(session.close(StatusCode.Registry.InternalError))
        }

      case false => req
