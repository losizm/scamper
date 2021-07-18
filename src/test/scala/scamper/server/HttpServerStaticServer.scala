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

import java.nio.file.Files

import scala.collection.immutable.ListMap
import scala.language.implicitConversions

import scamper.*
import scamper.Implicits.given
import scamper.client.HttpClient
import scamper.headers.*
import scamper.server.Implicits.given
import scamper.types.*
import scamper.types.Implicits.given

import ResponseStatus.Registry.*

class HttpServerStaticServerSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer:
  it should "serve files" in testStaticServer("files", false)

  it should "serve files with SSL/TLS" in testStaticServer("files", true)

  it should "serve resources" in testStaticServer("resources", false)

  it should "serve resources with SSL/TLS" in testStaticServer("resources", true)

  private given client: HttpClient =
    HttpClient
      .settings()
      .trust(Resources.truststore)
      .continueTimeout(1000)
      .create()

  private given parser: BodyParser[Array[Byte]] = BodyParser.bytes(32 * 1024)

  private def testStaticServer(kind: String, secure: Boolean): Unit =
    withServer(secure) { implicit server =>
      info(s"serve html $kind")
      client.get(s"$serverUri/$kind/riteshiff/home.html") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/html"))
        assert(res.as[Array[Byte]].toSeq == getBytes("home.html").toSeq)
      }

      client.get(s"$serverUri/$kind/riteshiff/documentation.html") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/html"))
        assert(res.as[Array[Byte]].toSeq == getBytes("documentation.html").toSeq)
      }

      client.get(s"$serverUri/$kind/riteshiff/download.html") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/html"))
        assert(res.as[Array[Byte]].toSeq == getBytes("download.html").toSeq)
      }

      info(s"serve css $kind")
      client.get(s"$serverUri/$kind/riteshiff/css/style.css") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/css"))
        assert(res.as[Array[Byte]].toSeq == getBytes("css/style.css").toSeq)
      }

      info(s"serve image $kind")
      client.get(s"$serverUri/$kind/riteshiff/images/logo.svg") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("image/svg+xml"))
        assert(res.as[Array[Byte]].toSeq == getBytes("images/logo.svg").toSeq)
      }

      info(s"serve text $kind")
      client.get(s"$serverUri/$kind/riteshiff/LICENSE.txt") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/plain"))
        assert(res.as[Array[Byte]].toSeq == getBytes("LICENSE.txt").toSeq)
      }
  }

  private def getBytes(path: String): Array[Byte] =
    Files.readAllBytes(Resources.riteshiff.toPath.resolve(path))
