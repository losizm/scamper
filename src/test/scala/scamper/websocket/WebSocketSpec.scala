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
package scamper.websocket

import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.{ GET, POST }
import scamper.ResponseStatus.Registry.SwitchingProtocols
import scamper.headers.{ Connection, Upgrade }
import scamper.types.Implicits._

class WebSocketSpec extends org.scalatest.FlatSpec {
  val req = GET("/websocket/example")
    .withUpgrade("websocket")
    .withConnection("Upgrade")
    .withSecWebSocketKey(generateWebSocketKey())
    .withSecWebSocketVersion("13")

  it should "check WebSocket request" in {
    assert(checkWebSocketRequest(req) == req)
    assert(checkWebSocketRequest(req) == req)

    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.withMethod(POST)) }
    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.removeUpgrade) }
    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.removeConnection) }
    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.removeSecWebSocketKey) }
    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.removeSecWebSocketVersion) }

    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.withUpgrade("no-websocket")) }
    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.withConnection("no-upgrade")) }
    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.withSecWebSocketKey("123456789012345")) }
    assertThrows[InvalidWebSocketRequest] { checkWebSocketRequest(req.withSecWebSocketVersion("14")) }
  }

  it should "check WebSocket response" in {
    val res = SwitchingProtocols()
      .withUpgrade("websocket")
      .withConnection("Upgrade")
      .withSecWebSocketAccept(acceptWebSocketKey(req.secWebSocketKey))

    assert(checkWebSocketHandshake(req, res))
    assert(!checkWebSocketHandshake(req, res.removeUpgrade))
    assert(!checkWebSocketHandshake(req, res.removeSecWebSocketAccept))

    assert(!checkWebSocketHandshake(req, res.withUpgrade("no-websocket")))
    assert(!checkWebSocketHandshake(req, res.withSecWebSocketAccept("123")))
  }
}
