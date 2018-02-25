package scamper

import org.scalatest.FlatSpec

class HttpMessageSpec extends FlatSpec {
  import HttpResponses._
  import Implicits._

  "HttpRequest" should "be created" in {
    val request = HttpRequest("GET", "/find?user=root").withHeader("Host" -> "localhost:8080")
    assert(request.method == "GET")
    assert(request.uri == "/find?user=root")
    assert(request.getHeaderValue("Host").contains("localhost:8080"))
  }

  "HttpResponse" should "be created" in {
    val response = SeeOther.withHeader("Location" -> "/find")
    assert(response.status == SeeOther.status)
    assert(response.getHeaderValue("Location").contains("/find"))
  }
}

