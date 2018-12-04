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

import org.scalatest.FlatSpec
import scamper.ImplicitConverters.stringToUri
import scamper.RequestMethods._
import scamper.ResponseStatuses._
import scamper.headers._

class HttpMessageSpec extends FlatSpec {
  "HttpRequest" should "be created with path" in {
    val req = GET("?user=root&group=wheel").withPath("/find")
    assert(req.method.name == "GET")
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.getQueryParamValue("user").contains("root"))
    assert(req.getQueryParamValue("group").contains("wheel"))
  }

  it should "be created with query parameters" in {
    val req = GET("/find").withQueryParams("user" -> "root", "group" -> "wheel")
    assert(req.method.name == "GET")
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.getQueryParamValue("user").contains("root"))
    assert(req.getQueryParamValue("group").contains("wheel"))
  }

  it should "be created with host" in {
    val req = GET("/find?user=root&group=wheel").withHost("localhost:8080")
    assert(req.method.name == "GET")
    assert(req.target.toString == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.getQueryParamValue("user").contains("root"))
    assert(req.getQueryParamValue("group").contains("wheel"))
    assert(req.host == "localhost:8080")
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
