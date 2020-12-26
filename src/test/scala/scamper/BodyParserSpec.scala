/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.io.EOFException

import scamper.Auxiliary.InputStreamType
import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry._
import scamper.ResponseStatus.Registry._
import scamper.headers._
import scamper.types.Implicits._

class BodyParserSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "BodyParser" should "parse response with text body" in {
    implicit val bodyParser = BodyParser.text()
    val body = Entity("Hello, world!")
    val message = Ok(body).setContentType("text/plain").setContentLength(body.getLength.get)

    assert(message.status == Ok)
    assert(message.contentType.isText)
    assert(message.contentType.mainType == "text")
    assert(message.contentType.subtype == "plain")
    assert(message.as[String] == "Hello, world!")
  }

  it should "parse response with chunked text body" in {
    implicit val bodyParser = BodyParser.text()
    val body = Entity("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")
    val message = Ok(body).setContentType("text/plain; charset=utf8").setTransferEncoding("chunked")

    assert(message.as[String] == "Hello, world!")
  }

  it should "detect truncation in chunked text body" in {
    implicit val bodyParser = BodyParser.text()
    val message = Ok(Entity("100\r\nHello, world!")).setContentType("text/plain; charset=utf8").setTransferEncoding("chunked")
    assertThrows[EOFException](message.as[String])
  }

  it should "parse request with form body" in {
    implicit val bodyParser = BodyParser.form()
    val body = Entity("id" -> "0", "name" -> "root")
    val request = Post("users").setBody(body).setContentLength(body.getLength.get)
    val form = request.as[Map[String, Seq[String]]]

    assert(form("id").head == "0")
    assert(form("name").head == "root")
  }

  it should "parse request with form body as query string" in {
    implicit val bodyParser = BodyParser.query()
    val body = Entity("id" -> "0", "name" -> "root")
    val request = Post("users").setBody(body).setContentLength(body.getLength.get)
    val form = request.as[QueryString]

    assert(form.get("id").contains("0"))
    assert(form.get("name").contains("root"))
  }

  it should "not parse response with large body" in {
    implicit val bodyParser = BodyParser.text(8)
    val body = Entity("Hello, world!")
    val message = Ok(body).setContentType("text/plain").setContentLength(body.getLength.get)

    assertThrows[ReadLimitExceeded](message.as[String])
  }

  it should "not parse response with large entity" in {
    implicit val bodyParser = BodyParser.text(256)
    val body = Entity(getResourceBytes("/test.html.gz"))

    assertThrows[EntityTooLarge] {
      Ok(body)
        .setContentType("text/html")
        .setContentEncoding("gzip")
        .setContentLength(body.getLength.get)
        .as[String]
    }
  }

  private def getResourceBytes(name: String): Array[Byte] = {
    val in = getClass.getResourceAsStream(name)
    try in.getBytes()
    finally in.close()
  }
}
