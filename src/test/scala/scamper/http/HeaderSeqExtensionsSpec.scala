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

class HeaderSeqExtensionsSpec extends org.scalatest.flatspec.AnyFlatSpec:
  private val headers = Seq(
    Header("Content-Type", "text/plain"),
    Header("Content-Length", 1024),
    Header("Set-Cookie", "sessionid=24f64a2707c24b53"),
    Header("set-cookie", "affinity=site-0100a")
  )

  private val default = Header("Host", "scamper.example.com")
  private def headerNotFound(name: String) = throw HeaderNotFound(name)

  it should "test for header" in {
    assert(headers.hasHeader("content-type"))
    assert(headers.hasHeader("content-length"))
    assert(headers.hasHeader("set-cookie"))
    assert(!headers.hasHeader("host"))
  }

  it should "get header" in {
    assert(headers.getHeader("content-type")   contains headers(0))
    assert(headers.getHeader("content-length") contains headers(1))
    assert(headers.getHeader("set-cookie")     contains headers(2))
    assert(headers.getHeader("host").isEmpty)
  }

  it should "get header or else default header" in {
    assert(headers.getHeaderOrElse("content-type", default)   == headers(0))
    assert(headers.getHeaderOrElse("content-length", default) == headers(1))
    assert(headers.getHeaderOrElse("set-cookie", default)     == headers(2))
    assert(headers.getHeaderOrElse("host", default)           == default)
  }

  it should "get header or else throw exception" in {
    assert(headers.getHeaderOrElse("content-type", throw HeaderNotFound("Content-Type"))     == headers(0))
    assert(headers.getHeaderOrElse("content-length", throw HeaderNotFound("Content-Length")) == headers(1))
    assert(headers.getHeaderOrElse("set-cookie", throw HeaderNotFound("Set-Cookie"))         == headers(2))
    assertThrows[HeaderNotFound](headers.getHeaderOrElse("host", throw HeaderNotFound("Host")))
  }

  it should "get header value" in {
    assert(headers.getHeaderValue("content-type")   contains headers(0).value)
    assert(headers.getHeaderValue("content-length") contains headers(1).value)
    assert(headers.getHeaderValue("set-cookie")     contains headers(2).value)
    assert(headers.getHeaderValue("host").isEmpty)
  }

  it should "get header value or else default header value" in {
    assert(headers.getHeaderValueOrElse("content-type", default.value)   == headers(0).value)
    assert(headers.getHeaderValueOrElse("content-length", default.value) == headers(1).value)
    assert(headers.getHeaderValueOrElse("set-cookie", default.value)     == headers(2).value)
    assert(headers.getHeaderValueOrElse("host", default.value)           == default.value)
  }

  it should "get header value or else throw exception" in {
    assert(headers.getHeaderValueOrElse("content-type", throw HeaderNotFound("Content-Type"))     == headers(0).value)
    assert(headers.getHeaderValueOrElse("content-length", throw HeaderNotFound("Content-Length")) == headers(1).value)
    assert(headers.getHeaderValueOrElse("set-cookie", throw HeaderNotFound("Set-Cookie"))         == headers(2).value)
    assertThrows[HeaderNotFound](headers.getHeaderValueOrElse("host", throw HeaderNotFound("Host")))
  }

  it should "get headers" in {
    assert(headers.getHeaders("content-type")   == Seq(headers(0)))
    assert(headers.getHeaders("content-length") == Seq(headers(1)))
    assert(headers.getHeaders("set-cookie")     == Seq(headers(2), headers(3)))
    assert(headers.getHeaders("host").isEmpty)
  }

  it should "get header values" in {
    assert(headers.getHeaderValues("content-type")   == Seq(headers(0).value))
    assert(headers.getHeaderValues("content-length") == Seq(headers(1).value))
    assert(headers.getHeaderValues("set-cookie")     == Seq(headers(2).value, headers(3).value))
    assert(headers.getHeaderValues("host").isEmpty)
  }
