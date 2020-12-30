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
package scamper.websocket

import java.util.concurrent.atomic.AtomicReference
import java.net.{ ServerSocket, Socket }

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.DurationInt
import scala.util.{ Try, Failure, Success }
import scala.util.Random.{ nextBytes => randomBytes }

import scamper.{ Auxiliary, Uri }
import scamper.websocket.StatusCode.Registry.{ GoingAway, MessageTooBig, NormalClosure }

class WebSocketSessionSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "end sessions with 'Normal Closure' (no compression)" in withSessions(false)(testNormalClosure)

  it should "end sessions with 'Normal Closure' (deflate)" in withSessions(true)(testNormalClosure)

  it should "end sessions with 'Message Too Big' (no compression)" in withSessions(false)(testMessageTooBig)

  it should "end sessions with 'Message Too Big' (deflate)" in withSessions(true)(testMessageTooBig)

  it should "end sessions with 'Going Away' (no compression)" in withSessions(false)(testGoingAway)

  it should "end sessions with 'Going Away' (deflate)" in withSessions(true)(testGoingAway)

  private val assertDelay = 250L;

  private def testNormalClosure(server: WebSocketSession, client: WebSocketSession): Unit = {
    val messages       = Seq("This is message #1.", "This is message #2.", "This is message #3.")
    val textMessages   = new ListBuffer[String]
    val binaryMessages = new ListBuffer[String]
    val pingMessages   = new ListBuffer[String]
    val pongMessages   = new ListBuffer[String]
    val serverClosure  = new AtomicReference[StatusCode]
    val clientClosure  = new AtomicReference[StatusCode]

    info("set up server session")
    assert(server.state == SessionState.Pending)

    server.onPing { message =>
      pingMessages += new String(message, "utf-8")
      server.pong(message)
    }

    server.onPong { message =>
      pongMessages += new String(message, "utf-8")
    }

    server.onBinary { message =>
      binaryMessages += new String(message, "utf-8")
    }

    server.onText(textMessages.+=)
    server.onClose(serverClosure.set)

    assert(server.state == SessionState.Pending)
    info("open server session")
    server.open()
    assert(server.state == SessionState.Open)

    info("set up client session")
    assert(client.state == SessionState.Pending)

    client.onPing { message =>
      pingMessages += new String(message, "utf-8")
      client.pong(message)
    }

    client.onPong { message =>
      pongMessages += new String(message, "utf-8")
    }

    client.onBinary { message =>
      binaryMessages += new String(message, "utf-8")
    }

    client.onText(textMessages.+=)
    client.onClose(clientClosure.set)

    assert(client.state == SessionState.Pending)
    info("open client session")
    client.open()
    assert(client.state == SessionState.Open)

    info("begin server send")
    messages.foreach(server.send)
    messages.foreach(message => server.send(message.getBytes("utf-8")))
    messages.foreach(message => server.ping(message.getBytes("utf-8")))

    Thread.sleep(assertDelay)
    assert(messages == textMessages.toSeq)
    assert(messages == binaryMessages.toSeq)
    assert(messages == pingMessages.toSeq)
    assert(messages == pongMessages.toSeq)

    // Reset test buffers
    textMessages.clear()
    binaryMessages.clear()
    pingMessages.clear()
    pongMessages.clear()

    info("begin client send")
    messages.foreach(client.send)
    messages.foreach(message => client.send(message.getBytes("utf-8")))
    messages.foreach(message => client.ping(message.getBytes("utf-8")))

    Thread.sleep(assertDelay)
    assert(messages == textMessages.toSeq)
    assert(messages == binaryMessages.toSeq)
    assert(messages == pingMessages.toSeq)
    assert(messages == pongMessages.toSeq)

    info("close client session")
    client.close()

    Thread.sleep(assertDelay)
    assert(client.state == SessionState.Closed)
    assert(clientClosure.get == NormalClosure)

    info("close server session")
    server.close()

    Thread.sleep(assertDelay)
    assert(server.state == SessionState.Closed)
    assert(serverClosure.get == NormalClosure)
  }

  private def testMessageTooBig(server: WebSocketSession, client: WebSocketSession): Unit = {
    val binaryMessages = new ListBuffer[Array[Byte]]
    val serverClosure  = new AtomicReference[StatusCode]
    val clientClosure  = new AtomicReference[StatusCode]

    info("set up server session")
    assert(server.state == SessionState.Pending)
    server.payloadLimit(1024)
    server.onClose(serverClosure.set)
    assert(server.state == SessionState.Pending)

    info("open server session")
    server.open()
    assert(server.state == SessionState.Open)

    info("set up client session")
    assert(client.state == SessionState.Pending)
    client.messageCapacity(8192)
    client.onBinary(binaryMessages.+=)
    client.onClose(clientClosure.set)
    assert(client.state == SessionState.Pending)

    info("open client session")
    client.open()
    assert(client.state == SessionState.Open)

    info("send messages")
    server.send(randomBytes(client.messageCapacity - 128))
    server.send(randomBytes(client.messageCapacity - 64))
    server.send(randomBytes(client.messageCapacity - 32))
    server.send(randomBytes(client.messageCapacity + 32))

    Thread.sleep(assertDelay)
    assert(binaryMessages.size == 3)
    assert(binaryMessages(0).size == client.messageCapacity - 128)
    assert(binaryMessages(1).size == client.messageCapacity - 64)
    assert(binaryMessages(2).size == client.messageCapacity - 32)
    assert(client.state == SessionState.Closed)
    assert(server.state == SessionState.Closed)
    assert(clientClosure.get == MessageTooBig)
    assert(serverClosure.get == MessageTooBig)
  }

  private def testGoingAway(server: WebSocketSession, client: WebSocketSession): Unit = {
    val messages      = Seq("This is message #1.", "This is message #2.", "This is message #3.")
    val textMessages  = new ListBuffer[String]
    val serverClosure = new AtomicReference[StatusCode]
    val clientClosure = new AtomicReference[StatusCode]

    info("set up server session")
    assert(server.state == SessionState.Pending)
    server.idleTimeout(500)
    server.onText(textMessages.+=)
    server.onClose(serverClosure.set)
    assert(server.state == SessionState.Pending)

    info("open server session")
    server.open()
    assert(server.state == SessionState.Open)

    info("set up client session")
    assert(client.state == SessionState.Pending)
    client.onClose(clientClosure.set)
    assert(client.state == SessionState.Pending)

    info("open client session")
    client.open()
    assert(client.state == SessionState.Open)

    info("send messages")
    messages.foreach(client.send)

    Thread.sleep(server.idleTimeout + 250)
    assert(messages == textMessages.toSeq)
    assert(client.state == SessionState.Closed)
    assert(server.state == SessionState.Closed)
    assert(clientClosure.get == GoingAway)
    assert(serverClosure.get == GoingAway)
  }

  private def withSessions[T](deflate: Boolean)(test: (WebSocketSession, WebSocketSession) => T): Unit = {
    implicit val executor = Auxiliary.executor

    var server: Socket = null
    var client: Socket = null

    val connection = new ServerSocket(0)

    try {
      val futureServer = Future {
        server = connection.accept()
        WebSocketSession.forServer(server, "server", Uri("/"), "13", deflate, None)
      }

      val futureClient = Future {
        client = new Socket("localhost", connection.getLocalPort)
        WebSocketSession.forClient(client, "client", Uri("/"), "13", deflate, None)
      }

      test(
        Await.result(futureServer, 5.seconds),
        Await.result(futureClient, 5.seconds)
      )
    } finally {
      Try(client.close())
      Try(server.close())
      Try(connection.close())
    }
  }
}
