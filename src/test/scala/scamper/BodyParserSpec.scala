package scamper

import org.scalatest.FlatSpec
import scamper.Implicits._

class BodyParserSpec extends FlatSpec {
  "A BodyParser" should "parse an HTTP message" in {
    implicit val bodyParser = BodyParser.text
    var response = HttpResponse(200).withBody("Hello, world!")
    assert(response.status == Status.Ok)
    assert(response.parse.get == "Hello, world!")

    response = response.withHeaders("Transfer-Encoding: chunked")
        .withBody("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")

    assert(response.parse.get == "Hello, world!")
  }
}

