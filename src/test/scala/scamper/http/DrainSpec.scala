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
import java.nio.BufferOverflowException

import scala.language.implicitConversions

import scamper.http.headers.*
import scamper.http.types.given

import RequestMethod.Registry.*
import ResponseStatus.Registry.*

class DrainSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "drain request" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val req  = Get("/pangram").setBody(body).setContentLength(body.knownSize.get)

    req.drain()
    assert(body.data.read() == -1)
    req.drain()
  }

  it should "drain response" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val res  = Ok(body).setContentLength(body.knownSize.get)

    res.drain()
    assert(body.data.read() == -1)
    res.drain()
  }

  it should "drain request to byte array" in {
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val req  = Get("/pangram").setBody(body).setContentLength(body.knownSize.get)
    val buf  = new Array[Byte](64)
    val len  = req.drain(buf)

    assert(text == String(buf, 0, len))
    assert(body.data.read() == -1)
    assert(req.drain(buf) == 0)
  }

  it should "drain response to byte array" in {
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val res  = Ok(body).setContentLength(body.knownSize.get)
    val buf  = new Array[Byte](64)
    val len  = res.drain(buf)

    assert(text == String(buf, 0, len))
    assert(body.data.read() == -1)
    assert(res.drain(buf) == 0)
  }

  it should "drain request to byte array using offset and length" in {
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val req  = Get("/pangram").setBody(body).setContentLength(body.knownSize.get)
    val buf  = new Array[Byte](80)
    val len  = req.drain(buf, 10, 70)

    assert(text == String(buf, 10, len))
    assert(body.data.read() == -1)
    assert(req.drain(buf) == 0)
  }

  it should "drain response to byte array using offset and length" in {
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val res  = Ok(body).setContentLength(body.knownSize.get)
    val buf  = new Array[Byte](80)
    val len  = res.drain(buf, 10, 70)

    assert(text == String(buf, 10, len))
    assert(body.data.read() == -1)
    assert(res.drain(buf) == 0)
  }

  it should "drain request to output stream" in {
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val req  = Get("/pangram").setBody(body).setContentLength(body.knownSize.get)

    assert(text == String(req.drain(ByteArrayOutputStream(), 1024).toByteArray))
    assert(body.data.read() == -1)
    req.drain()
  }

  it should "drain response to output stream" in {
    val text = "The quick brown fox jumps over the lazy dog."
    val body = Entity(text)
    val res  = Ok(body).setContentLength(body.knownSize.get)

    assert(text == String(res.drain(ByteArrayOutputStream(), 1024).toByteArray))
    assert(body.data.read() == -1)
    res.drain()
  }

  it should "not drain request because body too large" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val req  = Get("/pangram").setBody(body).setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](req.drain(20))
  }

  it should "not drain response because body too large" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val res  = Ok(body).setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](res.drain(20))
  }

  it should "not drain request to byte array because body too large" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val req  = Get("/pangram").setBody(body).setContentLength(body.knownSize.get)

    assertThrows[BufferOverflowException](req.drain(new Array[Byte](20)))
  }

  it should "not drain response to byte array because body too large" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val res  = Ok(body).setContentLength(body.knownSize.get)

    assertThrows[BufferOverflowException](res.drain(new Array[Byte](20)))
  }

  it should "not drain request to byte array using offset and length because body too large" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val req  = Get("/pangram").setBody(body).setContentLength(body.knownSize.get)

    assertThrows[BufferOverflowException](req.drain(new Array[Byte](256), 100, 20))
  }

  it should "not drain response to byte array using offset and length because body too large" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val res  = Ok(body).setContentLength(body.knownSize.get)

    assertThrows[BufferOverflowException](res.drain(new Array[Byte](256), 100, 20))
  }

  it should "not drain request to output stream because body too large" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val req  = Get("/pangram").setBody(body).setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](req.drain(ByteArrayOutputStream(), 20))
  }

  it should "not drain response to output stream because body too large" in {
    val body = Entity("The quick brown fox jumps over the lazy dog.")
    val res  = Ok(body).setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](res.drain(ByteArrayOutputStream(), 20))
  }
