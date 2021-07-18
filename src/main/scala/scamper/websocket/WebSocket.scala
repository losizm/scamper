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
package scamper.websocket

import java.security.{ MessageDigest, SecureRandom }

import scala.util.Try

import scamper.{ Base64, HttpMessage, HttpRequest, HttpResponse }
import scamper.ResponseStatus.Registry.*
import scamper.headers.{ Connection, Upgrade }

/** Provides utilities for WebSocket handshake. */
object WebSocket:
  private val random = SecureRandom()

  /** Globally Unique Identifier &ndash; 258EAFA5-E914-47DA-95CA-C5AB0DC85B11 */
  val guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

  /** Generates WebSocket key. */
  def generateKey(): String =
    val key = new Array[Byte](16)
    random.nextBytes(key)
    Base64.encodeToString(key)

  /**
   * Generates header value for Sec-WebSocket-Accept using supplied WebSocket
   * key.
   *
   * @param key WebSocket key
   */
  def acceptKey(key: String): String =
    Base64.encodeToString(hash(key + guid))

  /**
   * Tests request for WebSocket upgrade.
   *
   * @param req request
   */
  def isUpgrade(req: HttpRequest): Boolean =
    req.isGet && checkUpgrade(req)

  /**
   * Tests response for WebSocket upgrade.
   *
   * @param res response
   */
  def isUpgrade(res: HttpResponse): Boolean =
    res.status == SwitchingProtocols && checkUpgrade(res)

  /**
   * Checks for successful WebSocket handshake based on supplied request and
   * response.
   *
   * @param req WebSocket request
   * @param res WebSocket response
   *
   * @return `true` if handshake is successful; `false` otherwise
   */
  def checkHandshake(req: HttpRequest, res: HttpResponse): Boolean =
    isUpgrade(req) && isUpgrade(res) &&
      checkConnection(req) && checkConnection(res) &&
      checkWebSocketKey(req) && checkWebSocketAccept(res, req.secWebSocketKey)

  /**
   * Validates WebSocket request.
   *
   * @param req WebSocket request
   *
   * @throws InvalidWebSocketRequest if WebSocket request is invalid
   *
   * @return unmodified WebSocket request
   */
  def validate(req: HttpRequest): HttpRequest =
    if !req.isGet then
      throw InvalidWebSocketRequest(s"Invalid method for WebSocket request: ${req.method}")

    if !checkUpgrade(req) then
      throw InvalidWebSocketRequest("Missing or invalid header: Upgrade")

    if !checkConnection(req) then
      throw InvalidWebSocketRequest("Missing or invalid header: Connection")

    if !checkWebSocketKey(req) then
      throw InvalidWebSocketRequest("Missing or invalid header: Sec-WebSocket-Key")

    if !checkWebSocketVersion(req) then
      throw InvalidWebSocketRequest("Missing or invalid header: Sec-WebSocket-Version")

    //if (!checkWebSocketExtensions(req))
    //  throw InvalidWebSocketRequest("Invalid header: Sec-WebSocket-Extensions")

    req

  private def checkUpgrade(msg: HttpMessage): Boolean =
    msg.upgrade.exists { protocol =>
      protocol.name == "websocket" && protocol.version.isEmpty
    }

  private def checkConnection(msg: HttpMessage): Boolean =
    msg.connection.exists("upgrade".equalsIgnoreCase)

  private def checkWebSocketKey(req: HttpRequest): Boolean =
    req.getSecWebSocketKey.exists(checkWebSocketKeyValue)

  private def checkWebSocketKeyValue(key: String): Boolean =
    Try(Base64.decode(key).size == 16).getOrElse(false)

  private def checkWebSocketVersion(msg: HttpMessage): Boolean =
    msg.getSecWebSocketVersion.contains("13")

  private def checkWebSocketExtensions(msg: HttpMessage): Boolean =
    msg.secWebSocketExtensions.forall { ext =>
      ext.identifier.matches("permessage-deflate|x-webkit-deflate-frame")
    }

  private def checkWebSocketAccept(res: HttpResponse, key: String): Boolean =
    res.getSecWebSocketAccept.exists(checkWebSocketAcceptValue(_, key))

  private def checkWebSocketAcceptValue(value: String, key: String): Boolean =
    value == acceptKey(key)

  private def hash(value: String): Array[Byte] =
    MessageDigest.getInstance("SHA-1").digest(value.getBytes("utf-8"))

  private[scamper] def enablePermessageDeflate(req: HttpRequest): Boolean =
    req.secWebSocketExtensions.exists { ext =>
      ext.identifier == "permessage-deflate" && ext.params.forall {
        case ("client_no_context_takeover", None) => true
        case ("server_no_context_takeover", None) => true
        case ("client_max_window_bits", _       ) => true
        case _ => false
      }
    }

  private[scamper] def enablePermessageDeflate(res: HttpResponse): Boolean =
    res.secWebSocketExtensions.exists { ext =>
      ext.identifier == "permessage-deflate" && ext.params.forall {
        case ("client_no_context_takeover", None) => true
        case ("server_no_context_takeover", None) => true
        case _ => false
      }
    }

  private[scamper] def enableWebkitDeflateFrame(msg: HttpMessage): Boolean =
    msg.secWebSocketExtensions.exists { ext =>
      ext.identifier == "x-webkit-deflate-frame" && ext.params.forall {
        case ("no_context_takeover", None) => true
        case _ => false
      }
    }
