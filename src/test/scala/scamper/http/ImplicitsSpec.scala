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

import java.io.File
import java.time.Instant

import scala.language.implicitConversions

import RequestMethod.Registry.Get
import ResponseStatus.Registry.Ok

class ImplicitsSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "String" should "be converted to URI" in {
    val uri: Uri = "https://localhost:8080/index.html?q=free"
    assert(uri == Uri("https://localhost:8080/index.html?q=free"))
  }

  it should "be converted to Header" in {
    val header: Header = "Host: localhost"
    assert(header == Header("Host", "localhost"))
  }

  it should "be converted to Entity" in {
    val entity: Entity = "Hello, world!"
    assert(entity.knownSize.contains("Hello, world!".length))
  }

  it should "be converted to RequestMethod" in {
    val method: RequestMethod = "GET"
    assert(method == Get)
  }

  "Int" should "be converted to ResponseStatus" in {
    val status: ResponseStatus = 200
    assert(status == Ok)
  }

  "File" should "be converted to Entity" in {
    val file = File("build.sbt")
    val entity: Entity = file
    assert(entity.knownSize.contains(file.length))
  }

  "Bytes" should "be converted to Entity" in {
    val bytes = "Hello, world!".getBytes("utf-8")
    val entity: Entity = bytes
    assert(entity.knownSize.contains(bytes.size))
  }

  "Tuple" should "be converted to Header" in {
    var header: Header = "Accept" -> "*/*"
    assert(header == Header("Accept", "*/*"))

    header = "Content-Length" -> 10
    assert(header == Header("Content-Length", 10))

    header = "Content-Length" -> 10L
    assert(header == Header("Content-Length", 10L))

    val now = Instant.now()
    header = "Date" -> now
    assert(header == Header("Date", now))
  }
