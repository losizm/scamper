package scamper

import org.scalatest.FlatSpec
import ImplicitConverters._
import ImplicitHeaders._

class BodyParserSpec extends FlatSpec with Statuses {
  "BodyParser" should "parse response with text body" in {
    implicit val bodyParser = BodyParser.text
    val body = Entity("Hello, world!")
    val message = Ok(body).withContentType("text/plain").withContentLength(body.length.get)

    assert(message.status == Ok)
    assert(message.contentType.isText)
    assert(message.contentType.mainType == "text")
    assert(message.contentType.subtype == "plain")
    assert(message.parse.get == "Hello, world!")
  }

  it should "parse response with chunked text body" in {
    implicit val bodyParser = BodyParser.text
    val message = Ok("7\r\nHello, \r\n6\r\nworld!\r\n0\r\n").withContentType("text/plain; charset=utf8").withTransferEncoding("chunked")

    assert(message.parse.get == "Hello, world!")
  }

  it should "parse request with form body" in {
    implicit val bodyParser = BodyParser.form
    val body = Entity("id=0&name=root")
    val request = HttpRequest("POST", "users").withContentLength(body.length.get).withBody(body)
    val form = request.parse.get

    assert(form("id").head == "0")
    assert(form("name").head == "root")
  }
}

