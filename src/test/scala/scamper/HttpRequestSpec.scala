package scamper

import org.scalatest.FlatSpec
import scamper.Implicits._

class HttpRequestSpec extends FlatSpec {
  "An HttpRequest" should "be created" in {
    val request = HttpRequest("GET", "/index.html?q=free").withHeaders("TE: chunked")
    assert(request.method == "GET")
    assert(request.uri == "/index.html?q=free")
    assert(request.path == "/index.html")
    assert(request.query.contains("q=free"))
    assert(request.getHeaderValue("te").contains("chunked"))
  }
}

