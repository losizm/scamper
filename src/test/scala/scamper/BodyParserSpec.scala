package scamper

import org.scalatest.FlatSpec
import Implicits._

class BodyParserSpec extends FlatSpec with Statuses {
  "BodyParser" should "parse response with text body" in {
    implicit val bodyParser = BodyParser.text
    val body = Entity("Hello, world!")
    val message = Ok(body).withHeaders("Content-Type" -> "text/plain", "Content-Length" -> body.length.get)

    assert(message.status == Ok)
    assert(message.getHeaderValue("Content-Type").contains("text/plain"))
    assert(message.parse.get == "Hello, world!")
  }

  it should "parse response with chunked text body" in {
    implicit val bodyParser = BodyParser.text
    val message = Ok("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n")
      .withHeaders("Content" -> "text/plain; charset=utf8", "Transfer-Encoding" -> "chunked")

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

