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

import java.io.{ File, FileInputStream }

import scala.language.implicitConversions

import scamper.http.headers.given

import ResponseStatus.Registry.*

class HttpResponseSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create with location" in {
    val res = SeeOther().setLocation("/find")
    assert(res.status == SeeOther)
    assert(res.location.toString == "/find")
  }

  it should "create with attributes" in {
    var res = Ok().setAttributes("id" -> 1, "name" -> "response").putAttributes("success" -> true)
    assert(res.status == Ok)
    assert(res.attributes.size == 3)
    assert(res.getAttribute[Int]("id").contains(1))
    assert(res.getAttribute[String]("name").contains("response"))
    assert(res.getAttributeOrElse("success", false))
    assert(res.getAttributeOrElse("answer", 45) == 45)
    assert(res.setAttributes(Map.empty[String, Any]).attributes.size == 0)

    res = res.removeAttributes("name")
    assert(res.attributes.size == 2)
    assert(res.attributes.contains("id"))
    assert(res.attributes.contains("success"))
    assert(!res.attributes.contains("name"))
  }

  it should "create HttpResponse and set body" in {
    val htmlFile = File("src/test/resources/test.html")

    val res1 = Ok().setBody("Response #1".getBytes("UTF-8"))
    assert(res1.status == Ok)
    assert(res1.body.toByteArray.sameElements("Response #1".getBytes("UTF-8")))

    val res2 = NotFound().setBody("Response #2")
    assert(res2.status == NotFound)
    assert(res2.body.toByteArray.sameElements("Response #2".getBytes("UTF-8")))

    val res3 = InternalServerError().setBody(htmlFile)
    assert(res3.status == InternalServerError)
    assert(res3.body.toByteArray.sameElements(htmlFile.getBytes()))

    val res4 = InternalServerError().setBody(FileInputStream(htmlFile))
    assert(res4.status == InternalServerError)
    assert(res4.body.toByteArray.sameElements(htmlFile.getBytes()))
  }

  it should "get default value if header not found" in {
    val server = Header("Server", "Scamper/x.x")
    val location = Header("Location", "/find")
    val res = SeeOther().putHeaders(server)

    assert(res.getHeaderOrElse("Server", throw HeaderNotFound("Server")) == server)
    assert(res.getHeaderValueOrElse("Server", throw HeaderNotFound("Server")) == server.value)

    assert(res.getHeaderOrElse("Location", location) == location)
    assert(res.getHeaderValueOrElse("Location", location.value) == location.value)
  }

  it should "throw exception if header not found" in {
    val res = SeeOther()
    assertThrows[HeaderNotFound](res.getHeaderOrElse("Server", throw HeaderNotFound("Server")))
    assertThrows[HeaderNotFound](res.getHeaderValueOrElse("Server", throw HeaderNotFound("Server")))
  }
