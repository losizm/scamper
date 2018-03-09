package scamper.types

import org.scalatest.FlatSpec

class CharsetRangeSpec extends FlatSpec {
  "CharsetRange" should "be created" in {
    var range = CharsetRange("utf-8")
    assert(range.charset == "utf-8")
    assert(range.weight == 1f)
    assert(!range.isWildcard)
    assert(range.toString == "utf-8; q=1.0")

    range = CharsetRange("utf-8; q=0.60")
    assert(range.charset == "utf-8")
    assert(range.weight == 0.6f)
    assert(!range.isWildcard)
    assert(range.toString == "utf-8; q=0.6")

    range = CharsetRange("*; q=1.0")
    assert(range.charset == "*")
    assert(range.weight == 1.0f)
    assert(range.isWildcard)
    assert(range.toString == "*; q=1.0")
  }

  it should "match Charset" in {
    assert(CharsetRange("utf-8").matches("UTF-8"))
    assert(CharsetRange("UTF-8; q=0.6").matches("utf-8"))
    assert(CharsetRange("*").matches("UTF-8"))
    assert(CharsetRange("*").matches("iso-8859-1"))
    assert(CharsetRange("*; q=0.001").matches("ascii"))
  }

  it should "not match Charset" in {
    assert(!CharsetRange("utf-8").matches("utf-16"))
    assert(!CharsetRange("utf-16be; q=0.6").matches("utf-16le"))
  }

  it should "be destructured" in {
    val range = CharsetRange("iso-8859-1; q=0.7")

    range match {
      case CharsetRange(charset, weight) =>
        assert(charset == range.charset)
        assert(weight == range.weight)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](CharsetRange("ascii; q"))
    assertThrows[IllegalArgumentException](CharsetRange("ascii; q="))
    assertThrows[IllegalArgumentException](CharsetRange("ascii; =0.1"))
  }
}

