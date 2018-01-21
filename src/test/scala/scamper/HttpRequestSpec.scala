package scamper

import org.scalatest.FlatSpec
import scamper.Implicits._

class HttpRequestSpec extends FlatSpec {
  "An HttpRequest" should "be created" in {
    val request = HttpRequest("GET", "/index.html").withHeaders("TE: chunked")
    assert(request.method == "GET")
    assert(request.uri == "/index.html")
    assert(request.getHeaderValue("te").contains("chunked"))
  }
}

