package scamper

import org.scalatest.FlatSpec
import ImplicitHeaders._

class HttpMessageSpec extends FlatSpec with Statuses {
  "HttpRequest" should "be created" in {
    val request = HttpRequest("GET", "/find?user=root").withHost("localhost:8080")
    assert(request.method == "GET")
    assert(request.uri == "/find?user=root")
    assert(request.host == "localhost:8080")
  }

  "HttpResponse" should "be created" in {
    val response = SeeOther().withLocation("/find")
    assert(response.status == SeeOther)
    assert(response.location == "/find")
  }
}

