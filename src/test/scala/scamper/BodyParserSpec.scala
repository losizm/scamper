/*
 * Copyright 2018 Carlos Conyers
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

import org.scalatest.FlatSpec

import scamper.ImplicitConverters.stringToURI
import scamper.RequestMethods._
import scamper.ResponseStatuses._
import scamper.headers._
import scamper.types.ImplicitConverters._

class BodyParserSpec extends FlatSpec {
  "BodyParser" should "parse response with text body" in {
    implicit val bodyParser = BodyParsers.text()
    val body = Entity("Hello, world!")
    val message = Ok(body).withContentType("text/plain").withContentLength(body.length.get)

    assert(message.status == Ok)
    assert(message.contentType.isText)
    assert(message.contentType.mainType == "text")
    assert(message.contentType.subtype == "plain")
    assert(message.parse[String] == "Hello, world!")
  }

  it should "parse response with chunked text body" in {
    implicit val bodyParser = BodyParsers.text()
    val body = Entity("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")
    val message = Ok(body).withContentType("text/plain; charset=utf8").withTransferEncoding("chunked")

    assert(message.parse[String] == "Hello, world!")
  }

  it should "detect truncation in chunked text body" in {
    implicit val bodyParser = BodyParsers.text()
    val message = Ok(Entity("100\r\nHello, world!")).withContentType("text/plain; charset=utf8").withTransferEncoding("chunked")
    assertThrows[EOFException](message.parse[String])
  }

  it should "parse request with form body" in {
    implicit val bodyParser = BodyParsers.form()
    val body = Entity("id=0&name=root")
    val request = POST("users").withBody(body).withContentLength(body.length.get)
    val form: Map[String, Seq[String]] = request.parse

    assert(form("id").head == "0")
    assert(form("name").head == "root")
  }

  it should "not parse response with large body" in {
    implicit val bodyParser = BodyParsers.text(8)
    val body = Entity("Hello, world!")
    val message = Ok(body).withContentType("text/plain").withContentLength(body.length.get)

    assertThrows[HttpException](message.parse[String])
  }
}
