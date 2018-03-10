package scamper.types

import org.scalatest.FlatSpec

class ContentCodingSpec extends FlatSpec {
  "ContentCoding" should "be created" in {
    var coding = ContentCoding("Compress")
    assert(coding.name == "compress")
    assert(coding.isCompress)
    assert(coding.toString == "compress")

    coding = ContentCoding("X-COMPRESS")
    assert(coding.name == "compress")
    assert(coding.isCompress)
    assert(coding.toString == "compress")

    coding = ContentCoding("GZIP")
    assert(coding.name == "gzip")
    assert(coding.isGzip)
    assert(coding.toString == "gzip")

    coding = ContentCoding("x-GZIP")
    assert(coding.name == "gzip")
    assert(coding.isGzip)
    assert(coding.toString == "gzip")
  }

  it should "be destructured" in {
    val coding = ContentCoding("Deflate")
    coding match { case ContentCoding(name) => assert(name == coding.name) }
  }

  it should "not be created with invalid name" in {
    assertThrows[IllegalArgumentException](ContentCoding("x gzip"))
    assertThrows[IllegalArgumentException](ContentCoding("gzip; q=1.0"))
  }
}

