package scamper

import org.scalatest.FlatSpec
import HttpResponses._
import Implicits._

class BodyParserSpec extends FlatSpec {
  "A BodyParser" should "parse a response with a text body" in {
    implicit val bodyParser = BodyParser.text
    val message = Ok.withContentType("text/plain").withBody("Hello, world!")

    assert(message.status == Ok.status)
    assert(message.contentType.contains(ContentType("text/plain")))
    assert(message.parse.get == "Hello, world!")
  }

  it should "parse a response with a chunked text body" in {
    implicit val bodyParser = BodyParser.text
    val message = Ok.withContentType("text/plain; charset=utf8")
      .withChunked(true)
      .withBody("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")

    assert(message.parse.get == "Hello, world!")
  }

  it should "parse a request with a form body" in {
    implicit val bodyParser = BodyParser.form
    val request = HttpRequest("POST", "users").withBody("id=0&name=root")
    val form = request.parse.get

    assert(form("id").head == "0")
    assert(form("name").head == "root")
  }
}

