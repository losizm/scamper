/*
 * Copyright 2022 Carlos Conyers
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

import java.io.ByteArrayOutputStream
import java.nio.{ BufferOverflowException, ByteBuffer }
import java.nio.channels.Channels

import scala.language.implicitConversions

import scamper.http.headers.given
import scamper.http.types.given

import RequestMethod.Registry.*
import ResponseStatus.Registry.*

class DrainSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "drain message" in {
    testDrain(Get("/"))
    testDrain(Ok())
  }

  it should "drain message to output stream" in {
    testDrainOutputStream(Get("/"))
    testDrainOutputStream(Ok())
  }

  it should "not drain request to output stream because body too large" in {
    testNotDrainOutputStream(Get("/"))
    testNotDrainOutputStream(Ok())
  }

  private def testDrain(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg  = seed.setBody(body).setContentLength(body.knownSize.get)

    msg.drain(80)
    assert(body.data.read() == -1)
    msg.drain(80)

  private def testDrainOutputStream(seed: HttpRequest | HttpResponse): Unit =
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val msg  = seed.setBody(body).setContentLength(body.knownSize.get)
    val out  = ByteArrayOutputStream()

    assert(msg.drain(out, 1024) == body.knownSize.get)
    assert(out.toString("UTF-8") == text)
    assert(body.data.read() == -1)
    msg.drain(80)

  private def testNotDrain(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg  = seed.setBody(body).setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](msg.drain(20))

  private def testNotDrainOutputStream(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg  = seed.setBody(body).setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](msg.drain(ByteArrayOutputStream(), 20))
