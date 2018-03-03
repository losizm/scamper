package scamper

import org.scalatest.FlatSpec
import ImplicitConverters._

class RequestHandlerSpec extends FlatSpec with Statuses {
  "RequestHandlerChain" should "be traversed and handle request" in {
    val chain = RequestHandlerChain(
      (req, next) => next(req.addHeaders("user: guest")),
      (req, next) => next(req.addHeaders("access: read")),
      (req, next) => {
        val user = req.getHeaderValue("user").get
        val access = req.getHeaderValue("access").get
        val body = Entity(s"Hello, $user. You have $access access.", "utf8")

        Ok(body).withHeader("Content-Length" -> body.length.get)
      }
    )

    val resp = chain(HttpRequest("GET"))
    assert(resp.status == Ok)
    assert(resp.parse(BodyParser.text).get == "Hello, guest. You have read access.")
  }

  it should "be exhausted and not handle request" in {
    val chain = RequestHandlerChain()
    assertThrows[HttpException](chain(HttpRequest("GET")))
  }
}

