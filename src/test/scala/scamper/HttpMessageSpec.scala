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

import scamper.Implicits.stringToUri
import scamper.RequestMethod.Registry._
import scamper.ResponseStatus.Registry._
import scamper.headers._

class HttpMessageSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "HttpRequest" should "be created with path" in {
    val req = GET("?user=root&group=wheel").withPath("/find")
    assert(req.method.name == "GET")
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
  }

  it should "be created with empty path" in {
    assert(GET("").path == "/")
    assert(GET("http://localhost:8080").path == "/")
    assert(GET("http://localhost:8080/index.html").withPath("").path == "/")

    assert(OPTIONS("").path == "*")
    assert(OPTIONS("http://localhost:8080").path == "*")
    assert(OPTIONS("http://localhost:8080/index.html").withPath("/").path == "/")
    assert(OPTIONS("http://localhost:8080/index.html").withPath("*").path == "*")
    assert(OPTIONS("http://localhost:8080/index.html").withPath("").path == "*")
  }

  it should "be created with query parameters" in {
    val req = GET("/find").withQuery("user" -> "root", "group" -> "wheel")
    assert(req.method.name == "GET")
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
    assert(req.attributes.isEmpty)
  }

  it should "be created with host" in {
    val req = GET("/find?user=root&group=wheel").withHost("localhost:8080")
    assert(req.method.name == "GET")
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
    assert(req.host == "localhost:8080")
    assert(req.attributes.isEmpty)
  }

  it should "be created with attributes" in {
    var req = GET("/").withAttributes("id" -> 1, "name" -> "request").withAttribute("success" -> true)
    assert(req.method == GET)
    assert(req.attributes.size == 3)
    assert(req.getAttribute[Int]("id").contains(1))
    assert(req.getAttribute[String]("name").contains("request"))
    assert(req.getAttributeOrElse("success", false))
    assert(req.getAttributeOrElse("answer", 45) == 45)
    assert(req.withAttributes(Map.empty[String, Any]).attributes.size == 0)

    req = req.removeAttributes("name")
    assert(req.attributes.size == 2)
    assert(req.attributes.contains("id"))
    assert(req.attributes.contains("success"))
    assert(!req.attributes.contains("name"))
  }

  it should "get default value if header not found" in {
    val userAgent = Header("User-Agent", "Scamper/x.x")
    val host = Header("Host", "localhost:8080")
    val req = GET("/find?user=root&group=wheel").withHeader(userAgent)

    assert(req.getHeaderOrElse("User-Agent", throw HeaderNotFound("User-Agent")) == userAgent)
    assert(req.getHeaderValueOrElse("User-Agent", throw HeaderNotFound("User-Agent")) == userAgent.value)

    assert(req.getHeaderOrElse("Host", host) == host)
    assert(req.getHeaderValueOrElse("Host", host.value) == "localhost:8080")
  }

  it should "throw exception if header not found" in {
    val req = GET("/find?user=root&group=wheel")
    assertThrows[HeaderNotFound](req.getHeaderOrElse("User-Agent", throw HeaderNotFound("User-Agent")))
    assertThrows[HeaderNotFound](req.getHeaderValueOrElse("User-Agent", throw HeaderNotFound("User-Agent")))
  }

  "HttpResponse" should "be created with location" in {
    val res = SeeOther().withLocation("/find")
    assert(res.status == SeeOther)
    assert(res.location.toString == "/find")
  }

  it should "be created with attributes" in {
    var res = Ok().withAttributes("id" -> 1, "name" -> "response").withAttribute("success" -> true)
    assert(res.status == Ok)
    assert(res.attributes.size == 3)
    assert(res.getAttribute[Int]("id").contains(1))
    assert(res.getAttribute[String]("name").contains("response"))
    assert(res.getAttributeOrElse("success", false))
    assert(res.getAttributeOrElse("answer", 45) == 45)
    assert(res.withAttributes(Map.empty[String, Any]).attributes.size == 0)

    res = res.removeAttributes("name")
    assert(res.attributes.size == 2)
    assert(res.attributes.contains("id"))
    assert(res.attributes.contains("success"))
    assert(!res.attributes.contains("name"))
  }

  it should "get default value if header not found" in {
    val server = Header("Server", "Scamper/x.x")
    val location = Header("Location", "/find")
    val res = SeeOther().withHeader(server)

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
}
