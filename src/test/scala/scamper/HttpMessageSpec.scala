package scamper

import org.scalatest.FlatSpec
import scamper.headers.{ Host, Location }

class HttpMessageSpec extends FlatSpec with Statuses {
  "HttpRequest" should "be created with path" in {
    val req = HttpRequest("GET", "?user=root&group=wheel").withPath("/find")
    assert(req.method == "GET")
    assert(req.uri == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.getQueryParamValue("user").contains("root"))
    assert(req.getQueryParamValue("group").contains("wheel"))
  }

  it should "be created with query parameters" in {
    val req = HttpRequest("GET", "/find").withQueryParams("user" -> "root", "group" -> "wheel")
    assert(req.uri == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.getQueryParamValue("user").contains("root"))
    assert(req.getQueryParamValue("group").contains("wheel"))
  }

  it should "be created with host" in {
    val req = HttpRequest("GET", "/find?user=root&group=wheel").withHost("localhost:8080")
    assert(req.method == "GET")
    assert(req.uri == "/find?user=root&group=wheel")
    assert(req.path == "/find")
    assert(req.getQueryParamValue("user").contains("root"))
    assert(req.getQueryParamValue("group").contains("wheel"))
    assert(req.host == "localhost:8080")
  }


  "HttpResponse" should "be created with location" in {
    val response = SeeOther().withLocation("/find")
    assert(response.status == SeeOther)
    assert(response.location == "/find")
  }
}

