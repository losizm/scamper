package scamper.types

import org.scalatest.FlatSpec

class ContentCodingRangeSpec extends FlatSpec {
  "ContentCodingRange" should "be created" in {
    var range = ContentCodingRange("GZIP; q=0.7")
    assert(range.name == "gzip")
    assert(range.isGzip)
    assert(range.weight == 0.7f)
    assert(range.toString == "gzip; q=0.7")

    range = ContentCodingRange("*; q=1.0")
    assert(range.name == "*")
    assert(range.isWildcard)
    assert(range.weight == 1.0f)
    assert(range.toString == "*")
  }

  it should "be destructured" in {
    val range = ContentCodingRange("""Deflate; q=1.7777""")

    range match {
      case ContentCodingRange(name, weight) =>
        assert(name == range.name)
        assert(weight == range.weight)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](ContentCodingRange("identity; q"))
    assertThrows[IllegalArgumentException](ContentCodingRange("identity; q="))
    assertThrows[IllegalArgumentException](ContentCodingRange("identity; =0.1"))
  }
}

