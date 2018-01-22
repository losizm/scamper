package scamper

import org.scalatest.FlatSpec
import scamper.Implicits._

class HttpRequestSpec extends FlatSpec {
  "An HttpRequest" should "be created" in {
    val request = HttpRequest("GET", "/find").withQuery("user=root").withHeaders("Host: localhost:8080")
    assert(request.method == "GET")
    assert(request.uri == "/find?user=root")
    assert(request.path == "/find")
    assert(request.query.contains("user=root"))
    assert(request.getQueryParameterValue("user").contains("root"))
    assert(request.host.contains("localhost:8080"))
  }
}

