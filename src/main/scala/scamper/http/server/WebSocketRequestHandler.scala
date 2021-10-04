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

import scala.util.Try

import scamper.http.websocket.{ StatusCode, WebSocket, WebSocketApplication }

import Validate.notNull

private class WebSocketRequestHandler(app: WebSocketApplication[?]) extends RequestHandler:
  def apply(req: HttpRequest): HttpMessage =
    WebSocket.isUpgrade(req) match
      case true =>
        WebSocketUpgrade(req) { session =>
          try
            app(session)
          catch case err: Exception =>
              Try(session.logger.error(s"Error in WebSocket application: $err", err))
              Try(session.close(StatusCode.Registry.InternalError))
        }

      case false => req

private object WebSocketRequestHandler:
  def apply(app: WebSocketApplication[?]): WebSocketRequestHandler =
    new WebSocketRequestHandler(notNull(app))
