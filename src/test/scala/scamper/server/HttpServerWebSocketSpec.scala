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
package scamper.server

import java.util.concurrent.atomic.AtomicReference

import scala.collection.immutable.ListMap
import scala.collection.mutable.ListBuffer

import scamper._
import scamper.Implicits._
import scamper.client.HttpClient
import scamper.headers._
import scamper.server.Implicits._
import scamper.types._
import scamper.types.Implicits._
import scamper.websocket._

import Auxiliary.UriType
import ResponseStatus.Registry._
import StatusCode.Registry._

class HttpServerWebSocketSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer {
  it should "complete chat session with Normal Closure" in testNormalClosure(false)

  it should "complete chat session with Normal Closure with SSL/TLS" in testNormalClosure(true)

  it should "complete chat session with Unsupported Data" in testUnsupportedData(false)

  it should "complete chat session with Unsupported Data with SSL/TLS" in testUnsupportedData(true)

  it should "complete chat session with Protocol Error" in testProtocolError(false)

  it should "complete chat session with Protocol Error with SSL/TLS" in testProtocolError(true)

  it should "complete chat session with Internal Error" in testInternalError(false)

  it should "complete chat session with Internal Error with SSL/TLS" in testInternalError(true)

  it should "complete chat session with Going Away" in testGoingAway(false)

  it should "complete chat session with Going Away with SSL/TLS" in testGoingAway(true)

  it should "complete chat session with Policy Violation" in testPolicyViolation(false)

  it should "complete chat session with Policy Violation with SSL/TLS" in testPolicyViolation(true)

  private val client =
    HttpClient
      .settings()
      .trust(new java.io.File("./src/test/resources/secure/truststore"))
      .create()

  private val messages     = new ListBuffer[String]
  private val pingData     = new ListBuffer[Byte]
  private val pongData     = new ListBuffer[Byte]
  private val pingReceived = new AtomicReference[Boolean]
  private val closure      = new AtomicReference[StatusCode]

  private def testNormalClosure(secure: Boolean) = withSession(secure) { session =>
    val conversation = ListMap(
      "Hi."                 -> "Hello.",
      "What is your name?"  -> "My name is Lupita.",
      "How are you?"        -> "I'm fine. How are you?",
      "Great."              -> "I don't understand.",
      "Do you like squash?" -> "No, not really.",
      "I am from Florida"   -> "I don't understand.",
      "Bye."                -> "See ya."
    )

    info("send messages")
    conversation.keys.foreach(session.send)

    Thread.sleep(100)
    assert(pingReceived.get)
    assert(pongData.toSeq == pingData.toSeq)
    assert(messages.toSeq == conversation.values.toSeq)

    info("close client session")
    session.close()

    Thread.sleep(100)
    assert(session.state == SessionState.Closed)
    assert(closure.get == NormalClosure)
  }

  private def testUnsupportedData(secure: Boolean) = withSession(secure) { session =>
    val conversation = ListMap(
      "Hi."          -> "Hello.",
      "How are you?" -> "I'm fine. How are you?",
      "Great."       -> "I don't understand."
    )

    info("send messages")
    conversation.keys.foreach(session.send)

    Thread.sleep(100)
    assert(pingReceived.get)
    assert(messages.toSeq == conversation.values.toSeq)

    info("send binary message")
    session.send(RandomBytes(16))

    Thread.sleep(100)
    assert(session.state == SessionState.Closed)
    assert(closure.get == UnsupportedData)
  }

  private def testProtocolError(secure: Boolean) = withSession(secure) { session =>
    val conversation = ListMap(
      "Hi."          -> "Hello.",
      "How are you?" -> "I'm fine. How are you?",
      "Great."       -> "I don't understand."
    )

    info("send messages")
    conversation.keys.foreach(session.send)

    Thread.sleep(100)
    assert(pingReceived.get)
    assert(messages.toSeq == conversation.values.toSeq)

    info("send random pong")
    session.pong(RandomBytes(16))

    Thread.sleep(100)
    assert(session.state == SessionState.Closed)
    assert(closure.get == ProtocolError)
  }

  private def testInternalError(secure: Boolean) = withSession(secure) { session =>
    val conversation = ListMap(
      "Hi."          -> "Hello.",
      "How are you?" -> "I'm fine. How are you?",
      "Great."       -> "I don't understand."
    )

    info("send messages")
    conversation.keys.foreach(session.send)

    Thread.sleep(100)
    assert(pingReceived.get)
    assert(messages.toSeq == conversation.values.toSeq)

    info("send bad message")
    session.send("I love you.")

    Thread.sleep(100)
    assert(session.state == SessionState.Closed)
    assert(closure.get == InternalError)
  }

  private def testGoingAway(secure: Boolean) = withSession(secure) { session =>
    val conversation = ListMap(
      "Hi."          -> "Hello.",
      "How are you?" -> "I'm fine. How are you?",
      "Great."       -> "I don't understand.",
      "You at?"      -> "I don't understand.",
      "I'm sleepy."  -> "Ok, goodbye."
    )

    info("send messages")
    conversation.keys.foreach(session.send)

    Thread.sleep(100)
    assert(pingReceived.get)
    assert(messages.toSeq == conversation.values.toSeq)

    Thread.sleep(100)
    assert(session.state == SessionState.Closed)
    assert(closure.get == GoingAway)
  }

  private def testPolicyViolation(secure: Boolean) = withSession(secure) { session =>
    info("send messages")
    (1 to 10).foreach(_ => session.send("Hi."))

    Thread.sleep(100)
    assert(pingReceived.get)
    assert(messages.toSeq == (1 to 9).map(_ => "Hello."))

    Thread.sleep(100)
    assert(session.state == SessionState.Closed)
    assert(closure.get == PolicyViolation)
  }

  private def withSession[T](secure: Boolean)(test: WebSocketSession => T): Unit =
    withServer(secure) { implicit server =>
      // Reset test data
      messages.clear()
      pingData.clear()
      pingData.appendAll(RandomBytes(32))
      pongData.clear()
      pingReceived.set(false)
      closure.set(Reserved)

      val scheme  = if (server.isSecure) "wss" else "ws"
      val session = client.websocket(s"${serverUri.setScheme(scheme)}/chat/lupita") { session =>
        info("set up client session")

        assert(session.state == SessionState.Pending)

        session.idleTimeout(1000)
        session.payloadLimit(1024)
        session.messageCapacity(8192)
        session.onText(messages.+=)
        session.onPong(data => pongData.appendAll(data))
        session.onClose(closure.set)

        session.onPing { data =>
          info(s"ping received")
          pingReceived.set(true)
          session.pong(data)
        }

        info("open client session")
        session.open()

        Thread.sleep(100)
        assert(session.state == SessionState.Open)

        info("send ping")
        session.ping(pingData.toArray)
        session
      }

      try
        test(session)
      finally
        session.close()
    }
}
