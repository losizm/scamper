package scamper

import org.scalatest.FlatSpec
import ImplicitConversions._

class HttpMessageSpec extends FlatSpec with Statuses {
  "HttpRequest" should "be created" in {
    val request = HttpRequest("GET", "/find?user=root").withHeader("Host" -> "localhost:8080")
    assert(request.method == "GET")
    assert(request.uri == "/find?user=root")
    assert(request.getHeaderValue("Host").contains("localhost:8080"))
  }

  "HttpResponse" should "be created" in {
    val response = SeeOther().withHeader("Location" -> "/find")
    assert(response.status == SeeOther)
    assert(response.getHeaderValue("Location").contains("/find"))
  }
}

