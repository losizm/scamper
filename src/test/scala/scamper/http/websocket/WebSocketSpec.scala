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
package websocket

import scala.language.implicitConversions

import scamper.http.headers.{ toConnection, toUpgrade }
import scamper.http.types.given

import RequestMethod.Registry.{ Get, Post }
import ResponseStatus.Registry.SwitchingProtocols

class WebSocketSpec extends org.scalatest.flatspec.AnyFlatSpec:
  val req = Get("/websocket/example")
    .setUpgrade("websocket")
    .setConnection("Upgrade")
    .setSecWebSocketKey(WebSocket.generateKey())
    .setSecWebSocketVersion("13")

  it should "check WebSocket request" in {
    assert(WebSocket.validate(req) == req)
    assert(WebSocket.validate(req) == req)

    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.setMethod(Post)) }
    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.upgradeRemoved) }
    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.connectionRemoved) }
    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.secWebSocketKeyRemoved) }
    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.secWebSocketVersionRemoved) }

    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.setUpgrade("no-websocket")) }
    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.setConnection("no-upgrade")) }
    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.setSecWebSocketKey("123456789012345")) }
    assertThrows[InvalidWebSocketRequest] { WebSocket.validate(req.setSecWebSocketVersion("14")) }
  }

  it should "check WebSocket response" in {
    val res = SwitchingProtocols()
      .setUpgrade("websocket")
      .setConnection("Upgrade")
      .setSecWebSocketAccept(WebSocket.acceptKey(req.secWebSocketKey))

    assert(WebSocket.checkHandshake(req, res))
    assert(!WebSocket.checkHandshake(req, res.upgradeRemoved))
    assert(!WebSocket.checkHandshake(req, res.secWebSocketAcceptRemoved))

    assert(!WebSocket.checkHandshake(req, res.setUpgrade("no-websocket")))
    assert(!WebSocket.checkHandshake(req, res.setSecWebSocketAccept("123")))
  }

  it should "parse WebSocket extension" in {
    val ext1 = WebSocketExtension.parse("foo")
    assert(ext1.identifier == "foo")
    assert(ext1.params.isEmpty)

    val ext2 = WebSocketExtension.parse("bar; baz=2")
    assert(ext2.identifier == "bar")
    assert(ext2.params.size == 1)
    assert(ext2.params.get("baz").contains(Some("2")))

    val ext3 = WebSocketExtension.parse("qux; quux; quuux=3")
    assert(ext3.identifier == "qux")
    assert(ext3.params.size == 2)
    assert(ext3.params.get("quux").contains(None))
    assert(ext3.params.get("quuux").contains(Some("3")))
  }

  it should "parse WebSocket extension list" in {
    val list1 = WebSocketExtension.parseAll("foo")
    assert(list1.size == 1)
    assert(list1(0).identifier == "foo")
    assert(list1(0).params.isEmpty)

    val list2 = WebSocketExtension.parseAll("foo, bar; baz=2")
    assert(list2.size == 2)
    assert(list2(0).identifier == "foo")
    assert(list2(0).params.isEmpty)
    assert(list2(1).identifier == "bar")
    assert(list2(1).params.size == 1)
    assert(list2(1).params.get("baz").contains(Some("2")))

    val list3 = WebSocketExtension.parseAll("foo, bar; baz=2, qux; quux; quuux=3")
    assert(list3.size == 3)
    assert(list3(0).identifier == "foo")
    assert(list3(0).params.isEmpty)
    assert(list3(1).identifier == "bar")
    assert(list3(1).params.size == 1)
    assert(list3(1).params.get("baz").contains(Some("2")))
    assert(list3(2).identifier == "qux")
    assert(list3(2).params.size == 2)
    assert(list3(2).params.get("quux").contains(None))
    assert(list3(2).params.get("quuux").contains(Some("3")))
  }

  it should "read single Sec-WebSocket-Extensions header" in {
    val list1 = Get("/websocket").putHeaders("Sec-WebSocket-Extensions" -> "foo").secWebSocketExtensions
    assert(list1.size == 1)
    assert(list1(0).identifier == "foo")
    assert(list1(0).params.isEmpty)

    val list2 = Get("/websocket").putHeaders("Sec-WebSocket-Extensions" -> "foo, bar; baz=2").secWebSocketExtensions
    assert(list2.size == 2)
    assert(list2(0).identifier == "foo")
    assert(list2(0).params.isEmpty)
    assert(list2(1).identifier == "bar")
    assert(list2(1).params.size == 1)
    assert(list2(1).params.get("baz").contains(Some("2")))

    val list3 = Get("/websocket").putHeaders("Sec-WebSocket-Extensions" -> "foo, bar; baz=2, qux; quux; quuux=3").secWebSocketExtensions
    assert(list3.size == 3)
    assert(list3(0).identifier == "foo")
    assert(list3(0).params.isEmpty)
    assert(list3(1).identifier == "bar")
    assert(list3(1).params.size == 1)
    assert(list3(1).params.get("baz").contains(Some("2")))
    assert(list3(2).identifier == "qux")
    assert(list3(2).params.size == 2)
    assert(list3(2).params.get("quux").contains(None))
    assert(list3(2).params.get("quuux").contains(Some("3")))
  }

  it should "read multiple Sec-WebSocket-Extensions headers" in {
    val list1 = Get("/websocket").setHeaders(
      "Sec-WebSocket-Extensions" -> "foo",
      "Sec-WebSocket-Extensions" -> "bar; baz=2"
    ).secWebSocketExtensions

    assert(list1.size == 2)
    assert(list1(0).identifier == "foo")
    assert(list1(0).params.isEmpty)
    assert(list1(1).identifier == "bar")
    assert(list1(1).params.size == 1)
    assert(list1(1).params.get("baz").contains(Some("2")))

    val list2 = Get("/websocket").setHeaders(
      "Sec-WebSocket-Extensions" -> "foo",
      "Sec-WebSocket-Extensions" -> "bar; baz=2",
      "Sec-WebSocket-Extensions" -> "qux; quux; quuux=3"
    ).secWebSocketExtensions

    assert(list2.size == 3)
    assert(list2(0).identifier == "foo")
    assert(list2(0).params.isEmpty)
    assert(list2(1).identifier == "bar")
    assert(list2(1).params.size == 1)
    assert(list2(1).params.get("baz").contains(Some("2")))
    assert(list2(2).identifier == "qux")
    assert(list2(2).params.size == 2)
    assert(list2(2).params.get("quux").contains(None))
    assert(list2(2).params.get("quuux").contains(Some("3")))

    val list3 = Get("/websocket").setHeaders(
      "Sec-WebSocket-Extensions" -> "foo, bar; baz=2",
      "Sec-WebSocket-Extensions" -> "qux; quux; quuux=3"
    ).secWebSocketExtensions

    assert(list3.size == 3)
    assert(list3(0).identifier == "foo")
    assert(list3(0).params.isEmpty)
    assert(list3(1).identifier == "bar")
    assert(list3(1).params.size == 1)
    assert(list3(1).params.get("baz").contains(Some("2")))
    assert(list3(2).identifier == "qux")
    assert(list3(2).params.size == 2)
    assert(list3(2).params.get("quux").contains(None))
    assert(list3(2).params.get("quuux").contains(Some("3")))

    val list4 = Get("/websocket").setHeaders(
      "Sec-WebSocket-Extensions" -> "foo",
      "Sec-WebSocket-Extensions" -> "bar; baz=2, qux; quux; quuux=3"
    ).secWebSocketExtensions

    assert(list4.size == 3)
    assert(list4(0).identifier == "foo")
    assert(list4(0).params.isEmpty)
    assert(list4(1).identifier == "bar")
    assert(list4(1).params.size == 1)
    assert(list4(1).params.get("baz").contains(Some("2")))
    assert(list4(2).identifier == "qux")
    assert(list4(2).params.size == 2)
    assert(list4(2).params.get("quux").contains(None))
    assert(list4(2).params.get("quuux").contains(Some("3")))
  }
