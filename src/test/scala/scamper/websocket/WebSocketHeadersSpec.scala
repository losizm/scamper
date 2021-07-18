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

import scala.language.implicitConversions

import scamper.{ HeaderNotFound, HttpException }
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry.{ Get, Post }
import scamper.ResponseStatus.Registry.Ok

class WebSocketHeadersSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create response with Sec-WebSocket-Accept header" in {
    val res1 = Ok()
    assert(!res1.hasSecWebSocketAccept)
    assertThrows[HeaderNotFound](res1.secWebSocketAccept)
    assert(res1.getSecWebSocketAccept.isEmpty)
    assert(res1.removeSecWebSocketAccept == res1)

    val res2 = res1.setSecWebSocketAccept("s3pPLMBiTxaQ9kYGzzhZRbK+xOo=")
    assert(res2.hasSecWebSocketAccept)
    assert(res2.secWebSocketAccept == "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=")
    assert(res2.getSecWebSocketAccept.contains("s3pPLMBiTxaQ9kYGzzhZRbK+xOo="))
    assert(res2.removeSecWebSocketAccept == res1)
    assert(res2.getHeaderValue("Sec-WebSocket-Accept").contains("s3pPLMBiTxaQ9kYGzzhZRbK+xOo="))
  }

  it should "create message with Sec-WebSocket-Extensions header" in {
    val req1 = Get("/")
    assert(!req1.hasSecWebSocketExtensions)
    assert(req1.secWebSocketExtensions.isEmpty)
    assert(req1.getSecWebSocketExtensions.isEmpty)
    assert(req1.removeSecWebSocketExtensions == req1)

    val req2 = req1.setSecWebSocketExtensions("permessage-deflate", "x-webkit-deflate-frame")
    assert(req2.hasSecWebSocketExtensions)
    assert(req2.secWebSocketExtensions == Seq(WebSocketExtension("permessage-deflate"), WebSocketExtension("x-webkit-deflate-frame")))
    assert(req2.getSecWebSocketExtensions.contains(Seq(WebSocketExtension("permessage-deflate"), WebSocketExtension("x-webkit-deflate-frame"))))
    assert(req2.removeSecWebSocketExtensions == req1)
    assert(req2.getHeaderValue("Sec-WebSocket-Extensions").contains("permessage-deflate, x-webkit-deflate-frame"))

    val res1 = Ok()
    assert(!res1.hasSecWebSocketExtensions)
    assert(res1.secWebSocketExtensions.isEmpty)
    assert(res1.getSecWebSocketExtensions.isEmpty)
    assert(res1.removeSecWebSocketExtensions == res1)

    val res2 = res1.setSecWebSocketExtensions("permessage-deflate", "x-webkit-deflate-frame")
    assert(res2.hasSecWebSocketExtensions)
    assert(res2.secWebSocketExtensions == Seq(WebSocketExtension("permessage-deflate"), WebSocketExtension("x-webkit-deflate-frame")))
    assert(res2.getSecWebSocketExtensions.contains(Seq(WebSocketExtension("permessage-deflate"), WebSocketExtension("x-webkit-deflate-frame"))))
    assert(res2.removeSecWebSocketExtensions == res1)
    assert(res2.getHeaderValue("Sec-WebSocket-Extensions").contains("permessage-deflate, x-webkit-deflate-frame"))
  }

  it should "create request with Sec-WebSocket-Key header" in {
    val req1 = Get("/")
    assert(!req1.hasSecWebSocketKey)
    assertThrows[HeaderNotFound](req1.secWebSocketKey)
    assert(req1.getSecWebSocketKey.isEmpty)
    assert(req1.removeSecWebSocketKey == req1)

    val req2 = req1.setSecWebSocketKey("dGhlIHNhbXBsZSBub25jZQ==")
    assert(req2.hasSecWebSocketKey)
    assert(req2.secWebSocketKey == "dGhlIHNhbXBsZSBub25jZQ==")
    assert(req2.getSecWebSocketKey.contains("dGhlIHNhbXBsZSBub25jZQ=="))
    assert(req2.removeSecWebSocketKey == req1)
    assert(req2.getHeaderValue("Sec-WebSocket-Key").contains("dGhlIHNhbXBsZSBub25jZQ=="))
  }

  it should "create message with Sec-WebSocket-Protocol header" in {
    val req1 = Get("/")
    assert(!req1.hasSecWebSocketProtocol)
    assert(req1.secWebSocketProtocol.isEmpty)
    assert(req1.getSecWebSocketProtocol.isEmpty)
    assert(req1.removeSecWebSocketProtocol == req1)

    val req2 = req1.setSecWebSocketProtocol("chat", "superchat")
    assert(req2.hasSecWebSocketProtocol)
    assert(req2.secWebSocketProtocol == Seq("chat", "superchat"))
    assert(req2.getSecWebSocketProtocol.contains(Seq("chat", "superchat")))
    assert(req2.removeSecWebSocketProtocol == req1)
    assert(req2.getHeaderValue("Sec-WebSocket-Protocol").contains("chat, superchat"))

    val res1 = Ok()
    assert(!res1.hasSecWebSocketProtocol)
    assert(res1.secWebSocketProtocol.isEmpty)
    assert(res1.getSecWebSocketProtocol.isEmpty)
    assert(res1.removeSecWebSocketProtocol == res1)

    val res2 = res1.setSecWebSocketProtocol("chat", "superchat")
    assert(res2.hasSecWebSocketProtocol)
    assert(res2.secWebSocketProtocol == Seq("chat", "superchat"))
    assert(res2.getSecWebSocketProtocol.contains(Seq("chat", "superchat")))
    assert(res2.removeSecWebSocketProtocol == res1)
    assert(res2.getHeaderValue("Sec-WebSocket-Protocol").contains("chat, superchat"))
  }

  it should "create request with Sec-WebSocket-Protocol-Client header" in {
    val req1 = Get("/")
    assert(!req1.hasSecWebSocketProtocolClient)
    assertThrows[HeaderNotFound](req1.secWebSocketProtocolClient)
    assert(req1.getSecWebSocketProtocolClient.isEmpty)
    assert(req1.removeSecWebSocketProtocolClient == req1)

    val req2 = req1.setSecWebSocketProtocolClient("chat")
    assert(req2.hasSecWebSocketProtocolClient)
    assert(req2.secWebSocketProtocolClient == "chat")
    assert(req2.getSecWebSocketProtocolClient.contains("chat"))
    assert(req2.removeSecWebSocketProtocolClient == req1)
    assert(req2.getHeaderValue("Sec-WebSocket-Protocol-Client").contains("chat"))
  }

  it should "create response with Sec-WebSocket-Protocol-Server header" in {
    val res1 = Ok()
    assert(!res1.hasSecWebSocketProtocolServer)
    assertThrows[HeaderNotFound](res1.secWebSocketProtocolServer)
    assert(res1.getSecWebSocketProtocolServer.isEmpty)
    assert(res1.removeSecWebSocketProtocolServer == res1)

    val res2 = res1.setSecWebSocketProtocolServer("chat")
    assert(res2.hasSecWebSocketProtocolServer)
    assert(res2.secWebSocketProtocolServer == "chat")
    assert(res2.getSecWebSocketProtocolServer.contains("chat"))
    assert(res2.removeSecWebSocketProtocolServer == res1)
    assert(res2.getHeaderValue("Sec-WebSocket-Protocol-Server").contains("chat"))
  }

  it should "create message with Sec-WebSocket-Version header" in {
    val req1 = Get("/")
    assert(!req1.hasSecWebSocketVersion)
    assertThrows[HeaderNotFound](req1.secWebSocketVersion)
    assert(req1.getSecWebSocketVersion.isEmpty)
    assert(req1.removeSecWebSocketVersion == req1)

    val req2 = req1.setSecWebSocketVersion("13")
    assert(req2.hasSecWebSocketVersion)
    assert(req2.secWebSocketVersion == "13")
    assert(req2.getSecWebSocketVersion.contains("13"))
    assert(req2.removeSecWebSocketVersion == req1)
    assert(req2.getHeaderValue("Sec-WebSocket-Version").contains("13"))

    val res1 = Ok()
    assert(!res1.hasSecWebSocketVersion)
    assertThrows[HeaderNotFound](res1.secWebSocketVersion)
    assert(res1.getSecWebSocketVersion.isEmpty)
    assert(res1.removeSecWebSocketVersion == res1)

    val res2 = res1.setSecWebSocketVersion("13")
    assert(res2.hasSecWebSocketVersion)
    assert(res2.secWebSocketVersion == "13")
    assert(res2.getSecWebSocketVersion.contains("13"))
    assert(res2.removeSecWebSocketVersion == res1)
    assert(res2.getHeaderValue("Sec-WebSocket-Version").contains("13"))
  }

  it should "create request with Sec-WebSocket-Version-Client header" in {
    val req1 = Get("/")
    assert(!req1.hasSecWebSocketVersionClient)
    assert(req1.secWebSocketVersionClient.isEmpty)
    assert(req1.getSecWebSocketVersionClient.isEmpty)
    assert(req1.removeSecWebSocketVersionClient == req1)

    val req2 = req1.setSecWebSocketVersionClient("13", "-09")
    assert(req2.hasSecWebSocketVersionClient)
    assert(req2.secWebSocketVersionClient == Seq("13", "-09"))
    assert(req2.getSecWebSocketVersionClient.contains(Seq("13", "-09")))
    assert(req2.removeSecWebSocketVersionClient == req1)
    assert(req2.getHeaderValue("Sec-WebSocket-Version-Client").contains("13, -09"))
  }

  it should "create response with Sec-WebSocket-Version-Server header" in {
    val res1 = Ok()
    assert(!res1.hasSecWebSocketVersionServer)
    assert(res1.secWebSocketVersionServer.isEmpty)
    assert(res1.getSecWebSocketVersionServer.isEmpty)
    assert(res1.removeSecWebSocketVersionServer == res1)

    val res2 = res1.setSecWebSocketVersionServer("13", "-09")
    assert(res2.hasSecWebSocketVersionServer)
    assert(res2.secWebSocketVersionServer == Seq("13", "-09"))
    assert(res2.getSecWebSocketVersionServer.contains(Seq("13", "-09")))
    assert(res2.removeSecWebSocketVersionServer == res1)
    assert(res2.getHeaderValue("Sec-WebSocket-Version-Server").contains("13, -09"))
  }
