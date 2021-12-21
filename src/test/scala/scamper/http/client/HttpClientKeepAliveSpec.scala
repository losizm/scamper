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
package client

import java.util.concurrent.atomic.AtomicInteger

import scala.language.implicitConversions

import scamper.http.headers.*
import scamper.logging.NullLogger
import scamper.http.server.{ HttpServer, ServerApplication }
import scamper.http.types.{ *, given }

import ResponseStatus.Registry.*

class HttpClientKeepAliveSpec extends org.scalatest.flatspec.AnyFlatSpec:
  private given BodyParser[Array[Byte]] = BodyParser.bytes(16 * 1024)
  private given BodyParser[String]      = BodyParser.string(16 * 1024)

  it should "send requests using persistent connections" in withServer { server =>
    given HttpClient = HttpClient.settings().keepAlive().create()

    val serverUrl = getUrl(server)
    val count     = AtomicInteger(0)

    info("Truncating connection queue")
    ConnectionManager.truncate()
    assert(ConnectionManager.isEmpty)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.size == 1)

    info(s"Sending request #${count.incrementAndGet()}")
    assert(send(s"$serverUrl/echo", "Hello, server!", Ok) == "Hello, server!")
    assert(ConnectionManager.size == 1)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.isEmpty)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/not-modified", NotModified)
    assert(ConnectionManager.size == 1)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/see-other", SeeOther)
    assert(ConnectionManager.isEmpty)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.size == 1)

    withServer { server =>
      val serverUrl = getUrl(server)

      info(s"Sending request #${count.incrementAndGet()} to other server")
      send(s"$serverUrl/ok", Ok)
      assert(ConnectionManager.size == 2)

      info(s"Sending request #${count.incrementAndGet()} to other server")
      send(s"$serverUrl/ok", Ok)
      assert(ConnectionManager.size == 2)
    }

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.size == 2)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.size == 1)

    info(s"Sending request #${count.incrementAndGet()}")
    assert(send(s"$serverUrl/echo", "Hello, server!", Ok) == "Hello, server!")
    assert(ConnectionManager.size == 2)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/internal-server-error", InternalServerError)
    assert(ConnectionManager.size == 1)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.size == 2)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/switching-protocols", SwitchingProtocols)
    assert(ConnectionManager.size == 1)

    info(s"Sending request #${count.incrementAndGet()}")
    assert(send(s"$serverUrl/echo", "Hello, server!", Ok) == "Hello, server!")
    assert(ConnectionManager.size == 2)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/not-found", NotFound)
    assert(ConnectionManager.size == 1)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.size == 2)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.size == 2)

    info(s"Sending request #${count.incrementAndGet()}")
    send(s"$serverUrl/ok", Ok)
    assert(ConnectionManager.size == 1)
  }

  private def send(url: String, status: ResponseStatus)(using client: HttpClient): Array[Byte] =
    assert(client.keepAlive)
    client.get(url) { res =>
      assert(res.status == status)
      res.as[Array[Byte]]
    }

  private def send(url: String, body: Entity, status: ResponseStatus)(using client: HttpClient): String =
    assert(client.keepAlive)
    client.post(url, body = body) { res =>
      assert(res.status == status)
      res.as[String]
    }

  private def getUrl(server: HttpServer): String =
    StringBuilder()
      .append(if server.isSecure then "https" else "http")
      .append("://")
      .append(server.host.getHostAddress)
      .append(":")
      .append(server.port)
      .toString()

  private def withServer[T](f: HttpServer => T): T =
    val server = ServerApplication()
      .logger(NullLogger)
      .keepAlive(30, 3)
      .get("/switching-protocols")(_ => SwitchingProtocols())
      .get("/ok")(_ => Ok())
      .get("/see-other")(_ => SeeOther("See other: /other/page.html"))
      .get("/not-modified")(_ => NotModified())
      .get("/bad-request")(_ => BadRequest("Your bad"))
      .get("/internal-server-error")(_ => InternalServerError("My bad"))
      .post("/echo")(doEcho)
      .create("localhost", 0)

    try f(server) finally server.close()

  private def doEcho(req: HttpRequest): HttpResponse =
    Ok(req.as[Array[Byte]]).setContentType("application/octet-stream")
