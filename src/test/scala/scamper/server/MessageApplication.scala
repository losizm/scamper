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

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable.LinkedHashMap

import scamper.{ BodyParser, HttpRequest, HttpMessage }
import scamper.Implicits._
import scamper.ResponseStatus.Registry._
import scamper.headers.{ ContentType, Location }
import scamper.server.Implicits._
import scamper.types.Implicits._

object MessageApplication extends RoutingApplication {
  private implicit val bodyParser = BodyParser.text(8192)

  def apply(router: Router): Unit = {
    val messages = new LinkedHashMap[Int, String]
    val sequence = new AtomicInteger

    router.get("/") { implicit req =>
      withErrorHandling {
        val offset = getQueryInt("offset", 0)
        val limit  = getQueryInt("limit", messages.size)

        val outMessages = messages
          .drop(offset)
          .take(limit)
          .map { case (id, message) => s"$id: $message" }
          .mkString("\r\n\r\n")

        Ok(outMessages)
          .setContentType("text/plain")
      }
    }

    router.post("/") { implicit req =>
      withErrorHandling {
        val msg = getTextBody
        val id  = sequence.incrementAndGet()

        messages.put(id, msg)

        Created("Message posted.")
          .setContentType("text/plain")
          .setLocation(router.toAbsolutePath(s"/$id"))
      }
    }

    router.get("/:id") { implicit req =>
      withErrorHandling {
        val id = getPathInt("id")

        messages
          .get(id)
          .map(Ok(_))
          .getOrElse(NotFound(s"Message not found: $id"))
          .setContentType("text/plain")
      }
    }

    router.put("/:id") { implicit req =>
      withErrorHandling {
        val id  = getPathInt("id")
        val msg = getTextBody

        messages
          .put(id, msg)
          .map(_ => Ok("Message updated."))
          .getOrElse(NotFound(s"Message not found: $id"))
          .setContentType("text/plain")
      }
    }

    router.delete("/:id") { implicit req =>
      withErrorHandling {
        val id = getPathInt("id")

        messages
          .remove(id)
          .map(_ => Ok("Message deleted."))
          .getOrElse(NotFound(s"Message not found: $id"))
          .setContentType("text/plain")
      }
    }
  }

  private def getPathInt(name: String)(implicit req: HttpRequest): Int =
    req.params.getInt("id")

  private def getQueryInt(name: String, default: => Int = 0)(implicit req: HttpRequest): Int =
    try
      req
        .query
        .getInt(name)
        .getOrElse(default)
    catch {
      case _: NumberFormatException =>
        throw new ParameterNotConvertible(name, "__hidden__")
    }

  private def getTextBody(implicit req: HttpRequest): String =
    req
      .as[String]
      .replaceAll("\\s+", " ")
      .trim()

  private def withErrorHandling(message: => HttpMessage): HttpMessage =
    try
      message
    catch {
      case err: ParameterNotConvertible =>
        BadRequest("Invalid ${err.name}")
          .setContentType("text/plain")

      case _: Exception =>
        InternalServerError("Internal error encountered")
          .setContentType("text/plain")
    }
}
