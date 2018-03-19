package scamper

import org.scalatest.FlatSpec

class HttpVersionSpec extends FlatSpec {
  "HttpVersion" should "be created" in {
    assert(HttpVersion("1.0") == HttpVersion(1, 0))
    assert(HttpVersion("1.1") == HttpVersion(1, 1))
    assert(HttpVersion("2.0") == HttpVersion(2, 0))
    assert(HttpVersion("2") == HttpVersion(2, 0))
  }

  it should "be formatted" in {
    assert(HttpVersion("1.0").toString == "1.0")
    assert(HttpVersion("1.1").toString == "1.1")
    assert(HttpVersion("2").toString == "2.0")
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](HttpVersion("1."))
    assertThrows[IllegalArgumentException](HttpVersion(".1"))
    assertThrows[IllegalArgumentException](HttpVersion("1.a"))
    assertThrows[IllegalArgumentException](HttpVersion("a.1"))
    assertThrows[IllegalArgumentException](HttpVersion("a.a"))
    assertThrows[IllegalArgumentException](HttpVersion("2999999999.1"))
    assertThrows[IllegalArgumentException](HttpVersion("1.2999999999"))
    assertThrows[IllegalArgumentException](HttpVersion("2999999999.2999999999"))
  }
}

