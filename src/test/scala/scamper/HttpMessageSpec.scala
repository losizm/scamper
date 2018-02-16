package scamper

import org.scalatest.FlatSpec

class HttpMessageSpec extends FlatSpec {
  import HttpResponses._
  import Implicits._

  "HttpRequest" should "be created" in {
    val request = HttpRequest("GET", "/find").withQuery("user=root").withHeader("Host: localhost:8080")
    assert(request.method == "GET")
    assert(request.uri == "/find?user=root")
    assert(request.path == "/find")
    assert(request.query.contains("user=root"))
    assert(request.getQueryParameterValue("user").contains("root"))
    assert(request.host.contains("localhost:8080"))
  }

  "HttpResponse" should "be created" in {
    val response = SeeOther.withLocation("/find").withChunked(true)
    assert(response.status == SeeOther.status)
    assert(response.location.contains("/find"))
    assert(response.isChunked)
  }
}

