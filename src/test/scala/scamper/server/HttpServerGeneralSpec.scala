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
package scamper.server

import scala.language.implicitConversions

import scamper.{ *, given }
import scamper.client.{ ClientHttpRequest, HttpClient }
import scamper.headers.*
import scamper.types.{ *, given }

import RequestMethod.Registry.*
import ResponseStatus.Registry.*

class HttpServerGeneralSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer:
  it should "handle /home endpoint" in testHome(false)

  it should "handle /home endpoint with SSL/TLS" in testHome(true)

  it should "handle /about endpoint" in testAbout(false)

  it should "handle /about endpoint with SSL/TLS" in testAbout(true)

  it should "handle /echo endpoint" in testEcho(false)

  it should "handle /echo endpoint with SSL/TLS" in testEcho(true)

  it should "send 404 (Not Found)" in test404(false)

  it should "send 404 (Not Found) with SSL/TLS" in test404(true)

  it should "send 408 (Request Timeout)" in test408(false)

  it should "send 408 (Request Timeout) with SSL/TLS" in test408(true)

  it should "send 413 (Payload Too Large)" in test413(false)

  it should "send 413 (Payload Too Large) with SSL/TLS" in test413(true)

  it should "send 414 (URI Too Long)" in test414(false)

  it should "send 414 (URI Too Long) with SSL/TLS" in test414(true)

  it should "send 431 (Request Header Fields Too Large)" in test431(false)

  it should "send 431 (Request Header Fields Too Large) with SSL/TLS" in test431(true)

  it should "send 500 (Internal Server Error)" in test500(false)

  it should "send 500 (Internal Server Error) with SSL/TLS" in test500(true)

  it should "send 501 (Not Implemented)" in test501(false)

  it should "send 501 (Not Implemented) with SSL/TLS" in test501(true)

  private def testHome(secure: Boolean) = withServer(secure) { implicit server =>
    client.get(serverUri) { res =>
      assert(res.status == Ok)
      assert(!res.hasContentType)
      assert(res.connection == Seq("close"))
      assert(res.hasDate)
    }

    client.post(serverUri) { res =>
      assert(res.status == NotFound)
      assert(!res.hasContentType)
      assert(res.connection == Seq("close"))
      assert(res.hasDate)
    }

    client.put(serverUri) { res =>
      assert(res.status == NotFound)
      assert(!res.hasContentType)
      assert(res.connection == Seq("close"))
      assert(res.hasDate)
    }

    client.delete(serverUri) { res =>
      assert(res.status == NotFound)
      assert(!res.hasContentType)
      assert(res.connection == Seq("close"))
      assert(res.hasDate)
    }
  }

  private given client: HttpClient =
    HttpClient
      .settings()
      .trust(Resources.truststore)
      .continueTimeout(1000)
      .create()

  private given textBodyParser: BodyParser[String] = BodyParser.text(8192)
  private given byteBodyParser: BodyParser[Array[Byte]] = BodyParser.bytes(8192)

  private def testAbout(secure: Boolean) = withServer(secure) { implicit server =>
    client.get(s"$serverUri/about") { res =>
      assert(res.status == Ok)
      assert(res.contentType == MediaType("text/plain"))
      assert(res.connection == Seq("close"))
      assert(res.hasDate)
      assert(res.as[String] == "This is a test server.")
    }

    client.post(s"$serverUri/about") { res =>
      assert(res.status == NotFound)
      assert(!res.hasContentType)
      assert(res.connection == Seq("close"))
      assert(res.hasDate)
    }
  }

  private def testEcho(secure: Boolean) = withServer(secure) { implicit server =>
    client.post(s"$serverUri/echo", body = "Hello, world!") { res =>
      assert(res.status == Ok)
      assert(res.contentType == MediaType("application/octet-stream"))
      assert(res.connection == Seq("close"))
      assert(res.hasDate)
      assert(res.as[String] == "Hello, world!")
    }

    client.get(s"$serverUri/echo") { res =>
      assert(res.status == NotFound)
      assert(!res.hasContentType)
      assert(res.connection == Seq("close"))
      assert(res.hasDate)
    }
  }

  private def test404(secure: Boolean) = withServer(secure) { implicit server =>
    Get(s"$serverUri/path/to/nothing")
      .send { res =>
        assert(res.status == NotFound)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }
  }

  private def test408(secure: Boolean) = withServer(secure) { implicit server =>
    Post(s"$serverUri/echo")
      .setExpect("100-continue")
      .setBody("Hello, server!")
      .send { res =>
        assert(res.status == RequestTimeout)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }
  }

  private def test413(secure: Boolean) = withServer(secure) { implicit server =>
    Post(s"${serverUri}/echo")
      .setBody(RandomBytes(16 * 1024))
      .send { res =>
        assert(res.status == PayloadTooLarge)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }
  }

  private def test414(secure: Boolean) = withServer(secure) { implicit server =>
    Get(s"${serverUri}${"/test" * 1024}")
      .send { res =>
        assert(res.status == UriTooLong)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }
  }

  private def test431(secure: Boolean) = withServer(secure) { implicit server =>
    info("too many headers")
    Get(serverUri)
      .setHeaders((1 to 20).map(n => Header(s"Test-Header-$n", n)))
      .send { res =>
        assert(res.status == RequestHeaderFieldsTooLarge)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }

    info("header too large")
    Get(serverUri)
      .setHeaders(Header("Test-Header", "test" * 1024))
      .send { res =>
        assert(res.status == RequestHeaderFieldsTooLarge)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }
  }

  private def test500(secure: Boolean) = withServer(secure) { implicit server =>
    Get(s"$serverUri/throwException")
      .send { res =>
        assert(res.status == InternalServerError)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }
  }

  private def test501(secure: Boolean) = withServer(secure) { implicit server =>
    Get(s"$serverUri/notImplemented")
      .send { res =>
        assert(res.status == NotImplemented)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }

    Post(s"$serverUri/notImplemented")
      .send { res =>
        assert(res.status == NotImplemented)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }

    Put(s"$serverUri/notImplemented")
      .send { res =>
        assert(res.status == NotImplemented)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }

    Delete(s"$serverUri/notImplemented")
      .send { res =>
        assert(res.status == NotImplemented)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }

    RequestMethod("ANY")(s"$serverUri/notImplemented")
      .send { res =>
        assert(res.status == NotImplemented)
        assert(res.connection == Seq("close"))
        assert(res.hasDate)
      }
  }
