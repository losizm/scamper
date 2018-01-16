package scamper

import org.scalatest.FlatSpec

class VersionSpec extends FlatSpec {
  "A Version" should "be created" in {
    assert(Version("1.2") == Version(1, 2))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](Version("1"))
    assertThrows[IllegalArgumentException](Version("1."))
    assertThrows[IllegalArgumentException](Version(".1"))
    assertThrows[IllegalArgumentException](Version("1.a"))
    assertThrows[IllegalArgumentException](Version("a.1"))
    assertThrows[IllegalArgumentException](Version("a.a"))
    assertThrows[IllegalArgumentException](Version("2999999999.1"))
    assertThrows[IllegalArgumentException](Version("1.2999999999"))
    assertThrows[IllegalArgumentException](Version("2999999999.2999999999"))
  }
}

