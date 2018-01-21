package scamper

import org.scalatest.FlatSpec
import scamper.Implicits._

class BodyParserSpec extends FlatSpec {
  "A BodyParser" should "parse an HTTP message" in {
    implicit val bodyParser = BodyParser.text
    var response = HttpResponse(200).withBody(Entity("Hello, world!", "UTF-8"))
    assert(response.status == Status.Ok)
    assert(response.parse.get == "Hello, world!")

    response = response.withHeaders("Transfer-Encoding: chunked")
        .withBody(Entity("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n", "UTF-8"))

    assert(response.parse.get == "Hello, world!")
  }
}

