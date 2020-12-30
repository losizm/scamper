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

import scamper.{ Auxiliary, RandomBytes, Uri }
import scamper.websocket.StatusCode.Registry.{ GoingAway, MessageTooBig, NormalClosure }

class WebSocketSessionSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "end sessions with 'Normal Closure' (no compression)" in withSessions(false)(testNormalClosure)

  it should "end sessions with 'Normal Closure' (deflate)" in withSessions(true)(testNormalClosure)

  it should "end sessions with 'Message Too Big' (no compression)" in withSessions(false)(testMessageTooBig)

  it should "end sessions with 'Message Too Big' (deflate)" in withSessions(true)(testMessageTooBig)

  it should "end sessions with 'Going Away' (no compression)" in withSessions(false)(testGoingAway)

  it should "end sessions with 'Going Away' (deflate)" in withSessions(true)(testGoingAway)

  private val assertDelay    = 250L;
  private val textMessages   = new ListBuffer[String]
  private val binaryMessages = new ListBuffer[Array[Byte]]
  private val pingMessages   = new ListBuffer[Array[Byte]]
  private val pongMessages   = new ListBuffer[Array[Byte]]
  private val serverClosure  = new AtomicReference[StatusCode]
  private val clientClosure  = new AtomicReference[StatusCode]

  private def testNormalClosure(server: WebSocketSession, client: WebSocketSession): Unit = {
    val messages = Seq("This is message #1.", "This is message #2.", "This is message #3.")

    info("send server messages")
    messages.foreach(server.send)
    messages.foreach(message => server.send(message.getBytes("utf-8")))
    messages.foreach(message => server.ping(message.getBytes("utf-8")))

    Thread.sleep(assertDelay)
    assert(messages == textMessages.toSeq)
    assert(messages == binaryMessages.map(bytes => new String(bytes, "utf-8")).toSeq)
    assert(messages == pingMessages.map(bytes => new String(bytes, "utf-8")).toSeq)
    assert(messages == pongMessages.map(bytes => new String(bytes, "utf-8")).toSeq)

    // Reset test buffers
    textMessages.clear()
    binaryMessages.clear()
    pingMessages.clear()
    pongMessages.clear()

    info("send client messages")
    messages.foreach(client.send)
    messages.foreach(message => client.send(message.getBytes("utf-8")))
    messages.foreach(message => client.ping(message.getBytes("utf-8")))

    Thread.sleep(assertDelay)
    assert(messages == textMessages.toSeq)
    assert(messages == binaryMessages.map(bytes => new String(bytes, "utf-8")).toSeq)
    assert(messages == pingMessages.map(bytes => new String(bytes, "utf-8")).toSeq)
    assert(messages == pongMessages.map(bytes => new String(bytes, "utf-8")).toSeq)

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
    info("send messages")
    server.send(RandomBytes(client.messageCapacity - 128))
    server.send(RandomBytes(client.messageCapacity - 64))
    server.send(RandomBytes(client.messageCapacity - 32))
    server.send(RandomBytes(client.messageCapacity + 32))

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
    val messages = Seq("This is message #1.", "This is message #2.", "This is message #3.")

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
        createSession(server, "server", true, deflate)
      }

      val futureClient = Future {
        client = new Socket("localhost", connection.getLocalPort)
        createSession(client, "client", false, deflate)
      }

      // Reset test data
      textMessages.clear()
      binaryMessages.clear()
      pingMessages.clear()
      pongMessages.clear()
      serverClosure.set(null)
      clientClosure.set(null)

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

  private def createSession(socket: Socket, title: String, server: Boolean, deflate: Boolean): WebSocketSession = {
    val session = server match {
      case true  => WebSocketSession.forServer(socket, title, Uri("/"), "13", deflate, None)
      case false => WebSocketSession.forClient(socket, title, Uri("/"), "13", deflate, None)
    }

    info(s"set up $title session")
    assert(session.state == SessionState.Pending)

    session.idleTimeout(1000)
    session.payloadLimit(1024)
    session.messageCapacity(8192)
    session.onText(textMessages.+=)
    session.onBinary(message => binaryMessages += message.clone())
    session.onPong(message => pongMessages += message.clone())

    session.onPing { message =>
      pingMessages += message.clone()
      session.pong(message)
    }

    server match {
      case true  => session.onClose(serverClosure.set)
      case false => session.onClose(clientClosure.set)
    }

    assert(session.state == SessionState.Pending)
    info(s"open $title session")
    session.open()

    Thread.sleep(assertDelay)
    assert(session.state == SessionState.Open)

    session
  }
}
