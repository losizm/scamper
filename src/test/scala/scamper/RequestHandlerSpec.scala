package scamper

import org.scalatest.FlatSpec
import scamper.Implicits._

class RequestHandlerSpec extends FlatSpec {
  "A RequestHandlerChain" should "be traversed" in {
    val chain = RequestHandlerChain(
      (req, next) => next(req.addHeaders("user: guest")),
      (req, next) => next(req.addHeaders("access: read")),
      (req, next) => {
        val user = req.getHeaderValue("user").get
        val access = req.getHeaderValue("access").get

        Status.Ok.withBody(Entity(s"Hello, $user. You have $access access.", "utf8"))
      }
    )

    val resp = chain(HttpRequest("GET", "/"))

    assert(resp.status == Status.Ok)
    assert(resp.parse(BodyParser.text).get == "Hello, guest. You have read access.")
  }
}

