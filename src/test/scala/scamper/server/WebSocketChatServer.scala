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

import java.util.concurrent.atomic.AtomicInteger

import scamper.RandomBytes
import scamper.websocket._

import StatusCode.Registry._

class WebSocketChatServer(session: WebSocketSession) {
  private val messageCount   = new AtomicInteger(0)
  private val dontUnderstand = new AtomicInteger(0)
  private val pingData       = RandomBytes(16).toSeq

  session.idleTimeout(3000)
  session.payloadLimit(1024)
  session.messageCapacity(8192)
  session.onText(doText)
  session.onBinary(doBinary)
  session.onPing(doPing)
  session.onPong(doPong)
  session.onError(doError)
  session.open()

  // Start session with ping
  session.pingAsync(pingData.toArray)

  private def doText(message: String): Unit =
    messageCount.incrementAndGet() < 10 match {
      case true  => replyTo(message)
      case false => session.close(PolicyViolation)
    }

  private def doBinary(message: Array[Byte]): Unit =
    session.close(UnsupportedData)

  private def doPing(data: Array[Byte]): Unit =
    session.pongAsync(data)

  private def doPong(data: Array[Byte]): Unit =
    if (data.toSeq != pingData.toSeq)
      session.close(ProtocolError)

  private def doError(err: Throwable): Unit =
    session.close(InternalError)

  private def replyTo(message: String): Unit =
    message match {
      case "Hi." | "Hello."      => session.send("Hello.")
      case "What is your name?"  => session.send("My name is Lupita.")
      case "How are you?"        => session.send("I'm fine. How are you?")
      case "I am fine."          => session.send("Good to hear.")
      case "Do you like squash?" => session.send("No, not really.")
      case "I love you."         => throw new RuntimeException("Abort!")
      case "Bye." | "Goodbye."   => session.send("See ya."); session.close(NormalClosure)
      case _                     =>
        dontUnderstand.incrementAndGet() < 3 match {
          case true  => session.send("I don't understand.")
          case false => session.send("Ok, goodbye."); session.close(GoingAway)
        }
    }
}

object WebSocketChatServer extends (WebSocketSession => WebSocketChatServer) {
  def apply(session: WebSocketSession): WebSocketChatServer =
    new WebSocketChatServer(session)
}
