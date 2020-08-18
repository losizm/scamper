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

import scamper.Implicits.{ stringToUri, stringToHeader }
import scamper.RequestMethod.Registry._
import scamper.headers._

class HttpRequestSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "create HttpRequest with path" in {
    val req = Get("?user=root&group=wheel").withPath("/find")
    assert(req.method == Get)
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
  }

  it should "create HttpRequest with empty path" in {
    assert(Get("").path == "/")
    assert(Get("http://localhost:8080").path == "/")
    assert(Get("http://localhost:8080/index.html").withPath("").path == "/")

    assert(Options("").path == "*")
    assert(Options("http://localhost:8080").path == "*")
    assert(Options("http://localhost:8080/index.html").withPath("/").path == "/")
    assert(Options("http://localhost:8080/index.html").withPath("*").path == "*")
    assert(Options("http://localhost:8080/index.html").withPath("").path == "*")
  }

  it should "create HttpRequest with query parameters" in {
    val req = Get("/find").withQuery("user" -> "root", "group" -> "wheel")
    assert(req.method == Get)
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
    assert(req.attributes.isEmpty)
  }

  it should "create HttpRequest with host" in {
    val req = Get("/find?user=root&group=wheel").withHost("localhost:8080")
    assert(req.method == Get)
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.query.get("user").contains("root"))
    assert(req.query.get("group").contains("wheel"))
    assert(req.host == "localhost:8080")
    assert(req.attributes.isEmpty)
  }

  it should "create HttpRequest with attributes" in {
    var req = Get("/").withAttributes("id" -> 1, "name" -> "request").withAttribute("success" -> true)
    assert(req.method == Get)
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

  it should "create HttpRequest with optional header" in {
    var req1 = Get("/").withHeaders("Fixed: 0 ", "Sequence: 1", "Sequence: 2")

    val req2 = req1.withOptionalHeader("Sequence", Some("3"))
    assert(req2.headers.size == 2)
    assert(req2.getHeaderValues("Fixed") == Seq("0"))
    assert(req2.getHeaderValues("Sequence") == Seq("3"))

    val req3 = req1.withOptionalHeader("Sequence", None)
    assert(req3.headers.size == 1)
    assert(req3.getHeaderValues("Fixed") == Seq("0"))
    assert(req3.getHeaderValues("Sequence").isEmpty)

    val req4 = req1.addOptionalHeader("Sequence", Some("3"))
    assert(req4.headers.size == 4)
    assert(req4.getHeaderValues("Fixed") == Seq("0"))
    assert(req4.getHeaderValues("Sequence") == Seq("1", "2", "3"))

    val req5 = req1.addOptionalHeader("Sequence", None)
    assert(req5.headers.size == 3)
    assert(req5.getHeaderValues("Fixed") == Seq("0"))
    assert(req5.getHeaderValues("Sequence") == Seq("1", "2"))
  }

  it should "get default value if header not found" in {
    val userAgent = Header("User-Agent", "Scamper/x.x")
    val host = Header("Host", "localhost:8080")
    val req = Get("/find?user=root&group=wheel").withHeader(userAgent)

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
}
