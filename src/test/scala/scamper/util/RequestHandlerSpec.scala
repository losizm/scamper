package scamper.util

import org.scalatest.FlatSpec
import scamper.{ BodyParsers, Entity, HttpException, HttpRequest, Statuses }
import scamper.ImplicitConverters.{ stringToHeader, stringToRequestMethod, tupleToHeaderWithLongValue }

class RequestHandlerSpec extends FlatSpec with Statuses {
  "RequestHandlerChain" should "be traversed and response generated" in {
    implicit val bodyParser = BodyParsers.text(80)

    val handlers =  Seq[RequestHandler](
      req => Left(req.addHeaders("user: guest")),
      req => Left(req.addHeaders("access: read")),
      req => {
        val user = req.getHeaderValue("user").get
        val access = req.getHeaderValue("access").get
        val body = Entity(s"Hello, $user. You have $access access.", "utf8")

        Right(Ok(body).withHeader("Content-Length" -> body.length.get))
      },
      req => throw RequestNotSatisfied(req)
    )
    val resp = RequestHandlerChain.getResponse(HttpRequest("GET"), handlers)

    assert(resp.status == Ok)
    assert(resp.parse.get == "Hello, guest. You have read access.")
  }

  it should "be traversed and no response generated" in {
    assertThrows[RequestNotSatisfied](RequestHandlerChain.getResponse(HttpRequest("GET"), Nil))
  }
}

