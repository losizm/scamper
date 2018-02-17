package scamper

import org.scalatest.FlatSpec
import HttpResponses._
import Implicits._

class BodyParserSpec extends FlatSpec {
  "BodyParser" should "parse response with text body" in {
    implicit val bodyParser = BodyParser.text
    val body = Entity("Hello, world!")
    val message = Ok.withContentType("text/plain").withBody(body).withContentLength(body.length.get)

    assert(message.status == Ok.status)
    assert(message.contentType.contains(MediaType("text/plain")))
    assert(message.parse.get == "Hello, world!")
  }

  it should "parse response with chunked text body" in {
    implicit val bodyParser = BodyParser.text
    val message = Ok.withContentType("text/plain; charset=utf8")
      .withChunked(true)
      .withBody("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")

    assert(message.parse.get == "Hello, world!")
  }

  it should "parse request with form body" in {
    implicit val bodyParser = BodyParser.form
    val body = Entity("id=0&name=root")
    val request = HttpRequest("POST", "users").withBody(body).withContentLength(body.length.get)
    val form = request.parse.get

    assert(form("id").head == "0")
    assert(form("name").head == "root")
  }
}

