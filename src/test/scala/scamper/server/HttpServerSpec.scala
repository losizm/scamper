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

import scamper._
import scamper.Implicits._
import scamper.client.HttpClient
import scamper.headers._
import scamper.server.Implicits._
import scamper.types._
import scamper.types.Implicits._

import ResponseStatus.Registry._

class HttpServerSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer {
  private implicit val client         = HttpClient()
  private implicit val textBodyParser = BodyParser.text(8192)
  private implicit val byteBodyParser = BodyParser.bytes(8192)

  it should "test home endpoint" in withServer { implicit server =>
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

  it should "test about endpoint" in withServer { implicit server =>
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

  it should "test echo endpoint" in withServer { implicit server =>
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
}
