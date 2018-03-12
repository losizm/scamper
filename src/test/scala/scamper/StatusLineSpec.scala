package scamper

import org.scalatest.FlatSpec

class StatusLineSpec extends FlatSpec {
  "StatusLine" should "be created" in {
    var response = StatusLine("HTTP/1.1 200 OK")
    assert(response.version == Version("1.1"))
    assert(response.status == ResponseStatus(200, "OK"))

    response = StatusLine("HTTP/1.1 400 Bad Request")
    assert(response.version == Version("1.1"))
    assert(response.status == ResponseStatus(400, "Bad Request"))

    response = StatusLine("HTTP/1.1 500 Internal Server Error")
    assert(response.version == Version("1.1"))
    assert(response.status == ResponseStatus(500, "Internal Server Error"))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](StatusLine("HTTP/1.1"))
    assertThrows[IllegalArgumentException](StatusLine("HTTP/1.1 200"))
    assertThrows[IllegalArgumentException](StatusLine("HTTP/1.1 OK"))
  }
}

