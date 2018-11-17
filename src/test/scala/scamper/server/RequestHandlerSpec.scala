/*
 * Copyright 2018 Carlos Conyers
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

import org.scalatest.FlatSpec

import scamper.{ BodyParsers, Entity, HttpException, HttpRequest }
import scamper.ImplicitConverters.{ stringToHeader, stringToRequestMethod, tupleToHeaderWithLongValue }
import scamper.ResponseStatuses._

class RequestHandlerSpec extends FlatSpec {
  "RequestHandlerChain" should "be traversed and response generated" in {
    implicit val bodyParser = BodyParsers.text(80)

    val handlers =  Seq[RequestHandler](
      req => Left(req.addHeaders("user: guest")),
      req => Left(req.addHeaders("access: read")),
      req => {
        val user = req.getHeaderValue("user").get
        val access = req.getHeaderValue("access").get
        val body = Entity(s"Hello, $user. You have $access access.")

        Right(Ok(body).withHeader("Content-Length" -> body.length.get))
      },
      req => throw RequestNotSatisfied(req)
    )
    val resp = RequestHandlerChain.getResponse(HttpRequest("GET"), handlers)

    assert(resp.status == Ok)
    assert(resp.parse[String] == "Hello, guest. You have read access.")
  }

  it should "be traversed and no response generated" in {
    assertThrows[RequestNotSatisfied](RequestHandlerChain.getResponse(HttpRequest("GET"), Nil))
  }
}
