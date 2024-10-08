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

import java.io.{ BufferedReader, EOFException, InputStream }

import scala.language.implicitConversions

import scamper.http.headers.given
import scamper.http.types.given

import RequestMethod.Registry.*
import ResponseStatus.Registry.*

class BodyParserSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "parse response with string body" in {
    given BodyParser[String] = BodyParser.string()
    val body = Entity("Hello, world!")
    val message = Ok(body).setContentType("text/plain").setContentLength(body.knownSize.get)

    assert(message.status == Ok)
    assert(message.contentType.isText)
    assert(message.contentType.typeName == "text")
    assert(message.contentType.subtypeName == "plain")
    assert(message.as[String] == "Hello, world!")
  }

  it should "parse response with chunked string body" in {
    given BodyParser[String] = BodyParser.string()
    val body = Entity("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")
    val message = Ok(body).setContentType("text/plain; charset=utf8").setTransferEncoding("chunked")

    assert(message.as[String] == "Hello, world!")
  }

  it should "detect truncation in chunked string body" in {
    given BodyParser[String] = BodyParser.string()
    val message = Ok(Entity("100\r\nHello, world!")).setContentType("text/plain; charset=utf8").setTransferEncoding("chunked")
    assertThrows[EOFException](message.as[String])
  }

  it should "parse request with form body as query string" in {
    given BodyParser[QueryString] = BodyParser.queryString()
    val body = Entity(QueryString("id" -> "0", "name" -> "root"))
    val request = Post("users").setBody(body).setContentLength(body.knownSize.get)
    val form = request.as[QueryString]

    assert(form.get("id").contains("0"))
    assert(form.get("name").contains("root"))
  }

  it should "parse response to input stream" in {
    given BodyParser[InputStream] = BodyParser.inputStream()
    val body = Entity("Hello, world!")
    val message = Ok(body).setContentType("text/plain").setContentLength(body.knownSize.get)

    assert(message.status == Ok)
    assert(message.contentType.isText)
    assert(message.contentType.typeName == "text")
    assert(message.contentType.subtypeName == "plain")

    val in  = message.as[InputStream]
    val buf = new Array[Byte](256)
    val len = in.read(buf)

    assert(String(buf, 0, len, "utf-8") == "Hello, world!")
    assert(in.read() == -1)
  }

  it should "parse response to buffered reader" in {
    given BodyParser[BufferedReader] = BodyParser.reader()
    val body = Entity("Hello, world!")
    val message = Ok(body).setContentType("text/plain").setContentLength(body.knownSize.get)

    assert(message.status == Ok)
    assert(message.contentType.isText)
    assert(message.contentType.typeName == "text")
    assert(message.contentType.subtypeName == "plain")

    val in = message.as[BufferedReader]

    assert(in.readLine() == "Hello, world!")
    assert(in.readLine() == null)
  }

  it should "not parse response with large body" in {
    given BodyParser[String] = BodyParser.string(8)
    val body = Entity("Hello, world!")
    val message = Ok(body).setContentType("text/plain").setContentLength(body.knownSize.get)

    assertThrows[ReadLimitExceeded](message.as[String])
  }

  it should "not parse response with large entity" in {
    given BodyParser[String] = BodyParser.string(256)
    val body = Entity(getResourceBytes("/test.html.gz"))

    assertThrows[EntityTooLarge] {
      Ok(body)
        .setContentType("text/html")
        .setContentEncoding("gzip")
        .setContentLength(body.knownSize.get)
        .as[String]
    }
  }

  private def getResourceBytes(name: String): Array[Byte] =
    val in = getClass.getResourceAsStream(name)
    try in.readBytes()
    finally in.close()
