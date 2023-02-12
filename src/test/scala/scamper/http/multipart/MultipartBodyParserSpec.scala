/*
 * Copyright 2023 Carlos Conyers
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
package multipart

import java.io.File

import client.HttpClient
import server.{ *, given }

import RequestMethod.Registry.Post
import ResponseStatus.Registry.Ok

class MultipartBodyParserSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create and parse multipart message body" in  usingTestHttpServer { server =>
    info(s"Using $server")

    val client = HttpClient.settings()
      .resolveTo("localhost", server.port, false)
      .toHttpClient()

    val song = Multipart(
      Part("title", "Form Of Intellect"),
      Part("artist", "Gang Starr"),
      Part("album", "Step In The Arena"),
      Part("media", File("./src/test/resources/form_of_intellect.wav"))
    )

    val req = Post("/multipart").setMultipartBody(song)

    val partNames = client.send(req) { res =>
      given BodyParser[String] = BodyParser.string()

      res.as[String].split("\n").toSet
    }

    assert { partNames == Set("title", "artist", "album", "media") }
  }

  private def usingTestHttpServer[T](f: HttpServer => T): T =
    val server = HttpServer.app()
      .incoming(req => logMessage(req))
      .outgoing(res => logMessage(res))
      .post("/multipart")(readMultipart)
      .toHttpServer("localhost", 0)

    try f(server)
    finally server.close()

  private def logMessage[T <: HttpMessage](msg: T): T =
    info("")
    info(msg.startLine.toString)
    msg.headers.foreach(header => info(header.toString))
    info("")
    msg

  private def readMultipart(req: HttpRequest): HttpResponse =
    given BodyParser[Multipart] = Multipart.bodyParser()

    val multipart = req.as[Multipart]
    printSummary(multipart)
    Ok().setPlainBody(partNames(multipart))

  private def printSummary(multipart: Multipart): Unit =
    info("Multipart Summary")
    multipart.parts.map { part =>
      info(s"  name=${part.name}, size=${part.size}, contentType=${part.contentType}, contentDisposition=${part.contentDisposition}")
    }

  private def partNames(multipart: Multipart): String =
    multipart.parts.map(_.name).mkString("\n")
