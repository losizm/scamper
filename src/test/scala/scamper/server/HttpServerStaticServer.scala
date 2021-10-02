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
import scamper.types.{ *, given }

import ResponseStatus.Registry.*

class HttpServerStaticServerSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer:
  it should "serve files" in testStaticServer(false)

  it should "serve files with SSL/TLS" in testStaticServer(true)

  private given client: HttpClient =
    HttpClient
      .settings()
      .trust(Resources.truststore)
      .continueTimeout(1000)
      .create()

  private given BodyParser[Array[Byte]] = BodyParser.bytes(32 * 1024)
  private given BodyParser[String]      = BodyParser.text(32 * 1024)

  private def testStaticServer(secure: Boolean): Unit =
    withServer(secure) { implicit server =>
      info(s"serve default files")
      client.get(s"$serverUri/files/riteshiff") { res =>
        assert(res.status == SeeOther)
        assert(res.location == Uri("/files/riteshiff/home.html"))
        assert(res.as[String] == "See other: /files/riteshiff/home.html")
      }

      client.get(s"$serverUri/files/riteshiff/") { res =>
        assert(res.status == SeeOther)
        assert(res.location == Uri("/files/riteshiff/home.html"))
        assert(res.as[String] == "See other: /files/riteshiff/home.html")
      }

      info(s"serve html files")
      client.get(s"$serverUri/files/riteshiff/home.html") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/html"))
        assert(res.as[Array[Byte]].toSeq == getBytes("home.html").toSeq)
      }

      client.get(s"$serverUri/files/riteshiff/documentation.html") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/html"))
        assert(res.as[Array[Byte]].toSeq == getBytes("documentation.html").toSeq)
      }

      client.get(s"$serverUri/files/riteshiff/download.html") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/html"))
        assert(res.as[Array[Byte]].toSeq == getBytes("download.html").toSeq)
      }

      info(s"serve css files")
      client.get(s"$serverUri/files/riteshiff/css/style.css") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/css"))
        assert(res.as[Array[Byte]].toSeq == getBytes("css/style.css").toSeq)
      }

      info(s"serve image files")
      client.get(s"$serverUri/files/riteshiff/images/logo.svg") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("image/svg+xml"))
        assert(res.as[Array[Byte]].toSeq == getBytes("images/logo.svg").toSeq)
      }

      info(s"serve text files")
      client.get(s"$serverUri/files/riteshiff/LICENSE.txt") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/plain"))
        assert(res.as[Array[Byte]].toSeq == getBytes("LICENSE.txt").toSeq)
      }
  }

  private def getBytes(path: String): Array[Byte] =
    Files.readAllBytes(Resources.riteshiff.toPath.resolve(path))
