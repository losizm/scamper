package scamper

import org.scalatest.FlatSpec
import HttpResponses._
import Implicits._

class BodyParserSpec extends FlatSpec {
  "BodyParser" should "parse response with text body" in {
    implicit val bodyParser = BodyParser.text
    val body = Entity("Hello, world!")
    val message = Ok.withHeaders("Content-Type" -> "text/plain", "Content-Length" -> body.length.get).withBody(body)

    assert(message.status == Ok.status)
    assert(message.getHeaderValue("Content-Type").contains("text/plain"))
    assert(message.parse.get == "Hello, world!")
  }

  it should "parse response with chunked text body" in {
    implicit val bodyParser = BodyParser.text
    val message = Ok.withHeaders("Content" -> "text/plain; charset=utf8", "Transfer-Encoding" -> "chunked")
      .withBody("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")

    assert(message.parse.get == "Hello, world!")
  }

  it should "parse request with form body" in {
    implicit val bodyParser = BodyParser.form
    val body = Entity("id=0&name=root")
    val request = HttpRequest("POST", "users").withHeader("Content-Length" -> body.length.get).withBody(body)
    val form = request.parse.get

    assert(form("id").head == "0")
    assert(form("name").head == "root")
  }
}

