package scamper

import org.scalatest.FlatSpec

class HttpMessageSpec extends FlatSpec {
  import HttpResponses._

  "HttpRequest" should "be created" in {
    val request = HttpRequest("GET", "/find?user=root").withHost("localhost:8080")
    assert(request.method == "GET")
    assert(request.uri == "/find?user=root")
    assert(request.host.contains("localhost:8080"))
  }

  "HttpResponse" should "be created" in {
    val response = SeeOther.withLocation("/find")
    assert(response.status == SeeOther.status)
    assert(response.location.contains("/find"))
  }
}

