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
package server

import java.util.concurrent.atomic.AtomicInteger

import scala.language.implicitConversions

import scamper.http.client.HttpClient
import scamper.http.headers.*
import scamper.logging.NullLogger

import ResponseStatus.Registry.*

class HttpServerInternalRoutingSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "test internal routing" in withServer { server =>
    given BodyParser[String] = BodyParser.text(256)
    given BodyParser[Int]    = _.as[String].toInt

    val client  = HttpClient()
    val baseUri = s"http://localhost:${server.port}/messages"

    client.get(s"$baseUri/0") { req =>
      assert(req.status == NotFound)
      assert(req.getHeaderValue("Filtered").contains("1"))
      assert(req.as[Int] == 0)
    }

    client.get(s"$baseUri/1") { req =>
      assert(req.status == Ok)
      assert(req.getHeaderValue("Filtered").contains("2"))
      assert(req.as[Int] == 1)
    }

    client.get(s"$baseUri/passthrough/message") { req =>
      assert(req.status == NotFound)
      assert(req.getHeaderValue("Filtered").isEmpty)
      assert(req.as[String] == "")
    }

    client.get(s"$baseUri/NaN") { req =>
      assert(req.status == BadRequest)
      assert(req.getHeaderValue("Filtered").contains("3"))
      assert(req.as[String] == "/messages/NaN")
    }

    client.get(s"$baseUri/2") { req =>
      assert(req.status == Ok)
      assert(req.getHeaderValue("Filtered").contains("4"))
      assert(req.as[Int] == 2)
    }

    client.get(s"$baseUri/error") { req =>
      assert(req.status == InternalServerError)
      assert(req.getHeaderValue("Filtered").isEmpty)
      assert(req.as[String] == "")
    }

    client.get(s"$baseUri/3") { req =>
      assert(req.status == Ok)
      assert(req.getHeaderValue("Filtered").contains("5"))
      assert(req.as[Int] == 3)
    }

    client.get(s"$baseUri/4") { req =>
      assert(req.status == NotFound)
      assert(req.getHeaderValue("Filtered").contains("6"))
      assert(req.as[Int] == 4)
    }

    client.get(s"http://localhost:${server.port}/notfound") { req =>
      assert(req.status == NotFound)
      assert(req.getHeaderValue("Filtered").isEmpty)
      assert(req.as[String] == "")
    }
  }

  private def withServer[T](f: HttpServer => T): T =
    val server = HttpServer
      .app()
      .logger(NullLogger)
      .route("/messages")(doRouting)
      .create("localhost", 0)

    try
      f(server)
    finally
      server.close()

  private def doRouting(router: Router): Unit =
    val filtered = AtomicInteger(0)

    router.get("/passthrough/message") { req =>
      req
    }

    router.get("/error") { req =>
      throw RuntimeException("Uh oh!")
    }

    router.get("/:id") { req =>
      val id = req.params.getInt("id")

      id >= 1 && id <= 3 match
        case true  => Ok(id.toString)
        case false => NotFound(id.toString)
    }

    router.outgoing { res =>
      res.addHeaders(s"Filtered: ${filtered.incrementAndGet()}")
    }

    router.recover { req =>
      { case _: ParameterNotConvertible => BadRequest(req.target.toString) }
    }

