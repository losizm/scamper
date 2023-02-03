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

import scamper.http.headers.*

import RequestMethod.Registry.*

class HttpRequestSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create HttpRequest with path" in {
    val req = Get("?user=root&group=wheel").setPath("/find")
    assert(req.method == Get)
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
  }

  it should "create HttpRequest with empty path" in {
    assert(Get("").path == "/")
    assert(Get("http://localhost:8080").path == "/")
    assert(Get("http://localhost:8080/index.html").setPath("").path == "/")

    assert(Options("").path == "*")
    assert(Options("http://localhost:8080").path == "*")
    assert(Options("http://localhost:8080/index.html").setPath("/").path == "/")
    assert(Options("http://localhost:8080/index.html").setPath("*").path == "*")
    assert(Options("http://localhost:8080/index.html").setPath("").path == "*")
  }

  it should "create HttpRequest with query parameters" in {
    val req = Get("/find").setQuery("user" -> "root", "group" -> "wheel")
    assert(req.method == Get)
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
    assert(req.attributes.isEmpty)
  }

  it should "create HttpRequest with host" in {
    val req = Get("/find?user=root&group=wheel").setHost("localhost:8080")
    assert(req.method == Get)
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
    assert(req.host == "localhost:8080")
    assert(req.attributes.isEmpty)
  }

  it should "create HttpRequest with attributes" in {
    var req = Get("/").setAttributes("id" -> 1, "name" -> "request").putAttributes("success" -> true)
    assert(req.method == Get)
    assert(req.attributes.size == 3)
    assert(req.getAttribute[Int]("id").contains(1))
    assert(req.getAttribute[String]("name").contains("request"))
    assert(req.getAttributeOrElse("success", false))
    assert(req.getAttributeOrElse("answer", 45) == 45)
    assert(req.setAttributes(Map.empty[String, Any]).attributes.size == 0)

    req = req.removeAttributes("name")
    assert(req.attributes.size == 2)
    assert(req.attributes.contains("id"))
    assert(req.attributes.contains("success"))
    assert(!req.attributes.contains("name"))
  }

  it should "create HttpRequest and set body" in {
    val htmlFile = File("src/test/resources/test.html")

    val req1 = Post("/a").setBody("Request #1".getBytes("UTF-8"))
    assert(req1.target == Uri("/a"))
    assert(req1.method == Post)
    assert(req1.body.toByteArray.sameElements("Request #1".getBytes("UTF-8")))

    val req2 = Put("/b").setBody("Request #2")
    assert(req2.target == Uri("/b"))
    assert(req2.method == Put)
    assert(req2.body.toByteArray.sameElements("Request #2".getBytes("UTF-8")))

    val req3 = Post("/c").setBody(Entity(htmlFile))
    assert(req3.target == Uri("/c"))
    assert(req3.method == Post)
    assert(req3.body.toByteArray.sameElements(htmlFile.getBytes()))

    val req4 = Put("/d").setBody(FileInputStream(htmlFile))
    assert(req4.target == Uri("/d"))
    assert(req4.method == Put)
    assert(req4.body.toByteArray.sameElements(htmlFile.getBytes()))
  }

  it should "get default value if header not found" in {
    val userAgent = Header("User-Agent", "Scamper/x.x")
    val host = Header("Host", "localhost:8080")
    val req = Get("/find?user=root&group=wheel").putHeaders(userAgent)

    assert(req.getHeaderOrElse("User-Agent", throw HeaderNotFound("User-Agent")) == userAgent)
    assert(req.getHeaderValueOrElse("User-Agent", throw HeaderNotFound("User-Agent")) == userAgent.value)

    assert(req.getHeaderOrElse("Host", host) == host)
    assert(req.getHeaderValueOrElse("Host", host.value) == "localhost:8080")
  }

  it should "throw exception if header not found" in {
    val req = Get("/find?user=root&group=wheel")
    assertThrows[HeaderNotFound](req.getHeaderOrElse("User-Agent", throw HeaderNotFound("User-Agent")))
    assertThrows[HeaderNotFound](req.getHeaderValueOrElse("User-Agent", throw HeaderNotFound("User-Agent")))
  }

  it should "test request method" in {
    assert(Get("/").isGet)
    assert(Post("/").isPost)
    assert(Put("/").isPut)
    assert(Patch("/").isPatch)
    assert(Delete("/").isDelete)
    assert(Head("/").isHead)
    assert(Options("/").isOptions)
    assert(Trace("/").isTrace)
    assert(Connect("/").isConnect)
  }
