package scamper

import org.scalatest.FlatSpec

class HeaderSpec extends FlatSpec {
  "A Header" should "be created" in {
    val header = Header("Content-Type: text/plain")
    assert(header.key == "Content-Type")
    assert(header.value == "text/plain")
    assert(header == Header(header.toString))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](Header("Content-Type"))
    assertThrows[IllegalArgumentException](Header("text/plain"))
  }
}

