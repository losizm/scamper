package scamper

import org.scalatest.FlatSpec
import HttpResponses._
import Implicits._

class BodyParserSpec extends FlatSpec {
  "A BodyParser" should "parse an HTTP message" in {
    implicit val bodyParser = BodyParser.text
    var response = Ok.withBody("Hello, world!")
    assert(response.status == Ok.status)
    assert(response.parse.get == "Hello, world!")

    response = response.withHeaders("Transfer-Encoding: chunked")
        .withBody("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")

    assert(response.parse.get == "Hello, world!")
  }
}

