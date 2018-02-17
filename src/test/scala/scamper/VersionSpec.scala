package scamper

import org.scalatest.FlatSpec

class VersionSpec extends FlatSpec {
  "Version" should "be created" in {
    assert(Version("1.0") == Version(1, 0))
    assert(Version("1.1") == Version(1, 1))
    assert(Version("2.0") == Version(2, 0))
    assert(Version("2") == Version(2, 0))
  }

  it should "be formatted" in {
    assert(Version("1.0").toString == "1")
    assert(Version("1.1").toString == "1.1")
    assert(Version("2.0").toString == "2")
    assert(Version("2").toString == "2")
  }

  it should "not be created" in {
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

