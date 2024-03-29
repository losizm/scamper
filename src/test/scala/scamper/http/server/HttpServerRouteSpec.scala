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

import scala.collection.immutable.ListMap
import scala.language.implicitConversions

import scamper.http.client.HttpClient
import scamper.http.headers.given
import scamper.http.types.{ *, given }

import ResponseStatus.Registry.*

class HttpServerRouteSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer:
  it should "test message application" in testApplication(false)

  it should "test message application with SSL/TLS" in testApplication(true)

  private given client: HttpClient =
    HttpClient
      .settings()
      .trust(Resources.truststore)
      .toHttpClient()

  private given bodyParser: BodyParser[String] = BodyParser.string(8192)

  private def testApplication(secure: Boolean) = withServer(secure) { implicit server =>
    implicit object MessagesBodyParser extends BodyParser[Map[Int, String]]:
      private val message = """\s*(\d+):\s+(.+)\s*""".r

      def parse(msg: HttpMessage): Map[Int, String] =
        msg
          .as[String]
          .split("(\r\n)+")
          .collect { case message(id, text) => id.toInt -> text }
          .toMap

    val originalMessages = ListMap(
      1 -> "This is message #1.",
      2 -> "This is message #2.",
      3 -> "This is message #3.",
      4 -> "This is message #4.",
      5 -> "This is message #5."
    )

    val updatedMessages = ListMap(
      2 -> "This is an updated message #2.",
      3 -> "This is an updated message #3."
    )

    val deletedMessages = Seq(1, 4)

    info("retrieving messages")
    client.get(s"$serverUri/api/messages") { res =>
      assert(res.status == Ok)
      assert(res.contentType == MediaType("text/plain"))
      assert(res.connection == Seq("close"))
      assert(res.hasDate)

      val apiMessages = res.as[Map[Int, String]]
      assert(apiMessages.isEmpty)
    }

    info(s"posting messages")
    originalMessages.foreach {
      case (id, message) =>
        client.post(s"$serverUri/api/messages", body = message) { res =>
          assert(res.status == Created)
          assert(res.contentType == MediaType("text/plain"))
          assert(res.hasDate)
          assert(res.location == Uri(s"/api/messages/$id"))
          assert(res.as[String] == "Message posted.")
        }

        client.get(s"$serverUri/api/messages/$id") { res =>
          assert(res.status == Ok)
          assert(res.contentType == MediaType("text/plain"))
          assert(res.hasDate)
          assert(res.as[String] == message)
        }
    }

    info(s"retrieving messages")
    client.get(s"$serverUri/api/messages") { res =>
      assert(res.status == Ok)
      assert(res.contentType == MediaType("text/plain"))
      assert(res.connection == Seq("close"))
      assert(res.hasDate)

      val apiMessages = res.as[Map[Int, String]]
      assert(apiMessages.size == originalMessages.size)

      apiMessages.foreach {
        case (id, message) => assert(message == originalMessages(id))
      }
    }

    info(s"updating messages")
    updatedMessages.foreach {
      case (id, message) =>
        client.put(s"$serverUri/api/messages/$id", body = message) { res =>
          assert(res.status == Ok)
          assert(res.contentType == MediaType("text/plain"))
          assert(res.hasDate)
          assert(!res.hasLocation)
          assert(res.as[String] == "Message updated.")
        }

        client.get(s"$serverUri/api/messages/$id") { res =>
          assert(res.status == Ok)
          assert(res.contentType == MediaType("text/plain"))
          assert(res.hasDate)
          assert(res.as[String] == message)
        }
    }

    info(s"deleting messages")
    deletedMessages.foreach { id =>
      client.delete(s"$serverUri/api/messages/$id") { res =>
        assert(res.status == Ok)
        assert(res.contentType == MediaType("text/plain"))
        assert(res.hasDate)
        assert(!res.hasLocation)
        assert(res.as[String] == "Message deleted.")
      }

      client.get(s"$serverUri/api/messages/$id") { res =>
        assert(res.status == NotFound)
        assert(res.contentType == MediaType("text/plain"))
        assert(res.hasDate)
        assert(res.as[String] == s"Message not found: $id")
      }
    }

    info(s"retrieving messages")
    client.get(s"$serverUri/api/messages") { res =>
      assert(res.status == Ok)
      assert(res.contentType == MediaType("text/plain"))
      assert(res.connection == Seq("close"))
      assert(res.hasDate)

      val apiMessages = res.as[Map[Int, String]]
      assert(apiMessages.size == originalMessages.size - deletedMessages.size)

      deletedMessages.foreach { id =>
        assert(!apiMessages.contains(id))
      }
    }
  }
