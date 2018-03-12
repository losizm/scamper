package scamper

import org.scalatest.FlatSpec
import scamper.RequestMethods._

class RequestLineSpec extends FlatSpec {
  "RequestLine" should "be created" in {
    var request = RequestLine("GET / HTTP/1.1")
    assert(request.method == GET)
    assert(request.uri == "/")
    assert(request.version == Version("1.1"))

    request = RequestLine("GET /index.html HTTP/1.1")
    assert(request.method == GET)
    assert(request.uri == "/index.html")
    assert(request.version == Version("1.1"))

    request = RequestLine("GET /index.html?offset=25&limit=5 HTTP/1.1")
    assert(request.method == GET)
    assert(request.uri == "/index.html?offset=25&limit=5")
    assert(request.version == Version("1.1"))

    request = RequestLine("POST https://localhost:8787/admin/user/create HTTP/1.1")
    assert(request.method == POST)
    assert(request.uri == "https://localhost:8787/admin/user/create")
    assert(request.version == Version("1.1"))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](RequestLine("GET"))
    assertThrows[IllegalArgumentException](RequestLine("GET /index.html"))
    assertThrows[IllegalArgumentException](RequestLine("GET HTTP/1.1"))
  }
}

