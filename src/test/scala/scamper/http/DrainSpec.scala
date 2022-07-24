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

import scamper.http.headers.*
import scamper.http.types.given

import RequestMethod.Registry.*
import ResponseStatus.Registry.*

class DrainSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "drain message" in {
    testDrain(Get("/"))
    testDrain(Ok())
  }

  it should "drain message to byte array" in {
    testDrainByteArray(Get("/"))
    testDrainByteArray(Ok())

    testDrainByteArrayOffset(Get("/"))
    testDrainByteArrayOffset(Ok())
  }

  it should "drain message to byte buffer" in {
    testDrainByteBuffer(Get("/"))
    testDrainByteBuffer(Ok())
  }

  it should "drain message to output stream" in {
    testDrainOutputStream(Get("/"))
    testDrainOutputStream(Ok())
  }

  it should "drain message to channel" in {
    testDrainChannel(Get("/"))
    testDrainChannel(Ok())
  }

  it should "not drain message because body too large" in {
    testNotDrain(Get("/"))
    testNotDrain(Ok())
  }

  it should "not drain mesage to byte array because body too large" in {
    testNotDrainByteArray(Get("/"))
    testNotDrainByteArray(Ok())

    testNotDrainByteArrayOffset(Get("/"))
    testNotDrainByteArrayOffset(Ok())
  }

  it should "not drain mesage to byte buffer because body too large" in {
    testNotDrainByteBuffer(Get("/"))
    testNotDrainByteBuffer(Ok())
  }

  it should "not drain request to output stream because body too large" in {
    testNotDrainOutputStream(Get("/"))
    testNotDrainOutputStream(Ok())
  }

  it should "not drain request to channel because body too large" in {
    testNotDrainChannel(Get("/"))
    testNotDrainChannel(Ok())
  }

  private def testDrain(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)

    msg1.drain(80)
    assert(body.data.read() == -1)
    msg1.drain(80)

  private def testDrainByteArray(seed: HttpRequest | HttpResponse): Unit =
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)
    val buf  = new Array[Byte](80)
    val len  = msg1.drain(buf)

    assert(text == String(buf, 0, len))
    assert(body.data.read() == -1)
    assert(msg1.drain(buf) == 0)

  private def testDrainByteArrayOffset(seed: HttpRequest | HttpResponse): Unit =
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)
    val buf  = new Array[Byte](80)
    val len  = msg1.drain(buf, 10, 70)

    assert(text == String(buf, 10, len))
    assert(body.data.read() == -1)
    assert(msg1.drain(buf) == 0)

  private def testDrainByteBuffer(seed: HttpRequest | HttpResponse): Unit =
    val text  = "The quick brown fox jumps over the lazy dog."
    val body1 = Entity(text)
    val msg1  = seed.setBody(body1).setContentLength(body1.knownSize.get)
    val buf   = ByteBuffer.allocate(80)

    msg1.drain(buf).flip()
    assert(text == String(buf.array, 0, buf.limit))
    assert(body1.data.read() == -1)
    buf.clear()
    assert(msg1.drain(buf).position == 0)

    val body2 = Entity(text)
    val msg2  = seed.setBody(body2).setContentLength(body2.knownSize.get)

    buf.position(10).limit(60)
    msg2.drain(buf)
    assert(text == String(buf.array, 10, buf.position - 10))
    assert(body2.data.read() == -1)
    buf.clear()
    assert(msg1.drain(buf).position == 0)

  private def testDrainOutputStream(seed: HttpRequest | HttpResponse): Unit =
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)

    assert(text == String(msg1.drain(ByteArrayOutputStream(), 1024).toByteArray))
    assert(body.data.read() == -1)
    msg1.drain(80)

  private def testDrainChannel(seed: HttpRequest | HttpResponse): Unit =
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)
    val sink = ByteArrayOutputStream()

    msg1.drain(Channels.newChannel(sink), 1024)

    assert(text == String(sink.toByteArray))
    assert(body.data.read() == -1)
    msg1.drain(80)

  private def testNotDrain(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](msg1.drain(20))

  private def testNotDrainByteArray(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)

    assertThrows[BufferOverflowException](msg1.drain(new Array[Byte](20)))

  private def testNotDrainByteArrayOffset(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)

    assertThrows[BufferOverflowException](msg1.drain(new Array[Byte](256), 100, 20))

  private def testNotDrainByteBuffer(seed: HttpRequest | HttpResponse): Unit =
    val text = "The quick brown fox jumps over the lazy dog."
    val body1 = Entity(text)
    val msg1  = seed.setBody(body1).setContentLength(body1.knownSize.get)
    val buf1  = ByteBuffer.allocate(20)

    assertThrows[BufferOverflowException](msg1.drain(buf1))

    val body2 = Entity(text)
    val msg2  = seed.setBody(body2).setContentLength(body2.knownSize.get)
    val buf2  = ByteBuffer.allocate(80)
    buf2.position(25).limit(50)

    assertThrows[BufferOverflowException](msg2.drain(buf2))

  private def testNotDrainOutputStream(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](msg1.drain(ByteArrayOutputStream(), 20))

  private def testNotDrainChannel(seed: HttpRequest | HttpResponse): Unit =
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val msg1 = seed.setBody(body).setContentLength(body.knownSize.get)
    val sink = Channels.newChannel(ByteArrayOutputStream())

    assertThrows[ReadLimitExceeded](msg1.drain(sink, 20))
