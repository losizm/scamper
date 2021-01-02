/*
 * Copyright 2020 Carlos Conyers
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

import java.util.concurrent.atomic.AtomicReference

import scamper._
import scamper.Implicits._
import scamper.client.HttpClient
import scamper.headers.{ Server => _, _ }
import scamper.logging._
import scamper.server.Implicits._

import RequestMethod.Registry._
import ResponseStatus.Registry._

class HttpServerAttributesSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer {
  it should "check attributes" in testAttributes(false)

  it should "check attributes with SSL/TLS" in testAttributes(false)

  private implicit val parser = BodyParser.bytes(8192)
  private implicit val client =
    HttpClient
      .settings()
      .trust(Resources.truststore)
      .create()

  private val request  = new AtomicReference[HttpRequest]
  private val response = new AtomicReference[HttpResponse]

  private def testAttributes(secure: Boolean) = withServer(secure) { implicit server =>
    info("200 OK")
    client.get(serverUri) { res =>
      assert(res.status == Ok)
      assert(request.get.correlate != null)
      assert(request.get.requestCount == 1)
      assert(request.get.server eq server)
      assert(request.get.socket != null)

      assert(response.get.correlate == request.get.correlate)
      assert(response.get.requestCount == request.get.requestCount)
      assert(response.get.server eq request.get.server)
      assert(response.get.socket eq request.get.socket)
      assert(response.get.request.contains(request.get))
    }

    request.set(null)
    response.set(null)
    info("414 URI Too Long")
    client.get(s"${serverUri}${"/test" * 512}") { res =>
      assert(res.status == UriTooLong)
      assert(request.get == null)

      assert(response.get.correlate != null)
      assert(response.get.requestCount == 1)
      assert(response.get.server eq server)
      assert(response.get.socket != null)
      assert(response.get.request.isEmpty)
    }

    request.set(null)
    response.set(null)
    info("431 Request Header Fields Too Large (many headers)")
    client.get(serverUri, headers = Seq(Header("Test-Header", "test" * 512))) { res =>
      assert(res.status == RequestHeaderFieldsTooLarge)
      assert(request.get == null)

      assert(response.get.correlate != null)
      assert(response.get.requestCount == 1)
      assert(response.get.server eq server)
      assert(response.get.socket != null)
      assert(response.get.request.isEmpty)
    }

    request.set(null)
    response.set(null)
    info("431 Request Header Fields Too Large (large header)")
    client.get(serverUri, headers = (1 to 20).map(n => Header(s"Test-Header-$n", "test"))) { res =>
      assert(res.status == RequestHeaderFieldsTooLarge)
      assert(request.get == null)

      assert(response.get.correlate != null)
      assert(response.get.requestCount == 1)
      assert(response.get.server eq server)
      assert(response.get.socket != null)
      assert(response.get.request.isEmpty)
    }

    request.set(null)
    response.set(null)
    info("500 Internal Server Error")
    client.get(s"$serverUri/error") { res =>
      assert(res.status == InternalServerError)
      assert(request.get.correlate != null)
      assert(request.get.requestCount == 1)
      assert(request.get.server eq server)
      assert(request.get.socket != null)

      assert(response.get.correlate == request.get.correlate)
      assert(response.get.requestCount == request.get.requestCount)
      assert(response.get.server eq request.get.server)
      assert(response.get.socket eq request.get.socket)
      assert(response.get.request.contains(request.get))
    }
  }

  override def getServer(secure: Boolean = false, logging: Boolean = false): HttpServer = {
    val app =
      HttpServer
        .app()
        .bufferSize(1024)
        .headerLimit(10)
        .logger(if (logging) ConsoleLogger else NullLogger)
        .incoming { req => request.set(req.setBody(Entity.empty)); req }
        .incoming(_.putAttributes("after" -> 0))
        .incoming("/error") { _ => throw new Exception("Internal Error") }
        .incoming { req => req.as[Array[Byte]]; req }
        .incoming { _ => Ok() }
        .outgoing { res => response.set(res.setBody(Entity.empty)); res }
        .outgoing(_.putAttributes("after" -> 0))

    if (secure)
      app.secure(Resources.keystore, "letmein", "pkcs12")

    app.create("localhost", 0)
  }
}
