package scamper

import org.scalatest.FlatSpec

class TransferCodingSpec extends FlatSpec {
  "TransferCoding" should "be created without parameters" in {
    var coding = TransferCoding("CHUNKED")
    assert(coding.name == "chunked")
    assert(coding.isChunked)
    assert(coding.params.isEmpty)
    assert(coding.toString == "chunked")

    coding = TransferCoding("GZIP")
    assert(coding.name == "gzip")
    assert(coding.isGzip)
    assert(coding.params.isEmpty)
    assert(coding.toString == "gzip")
  }

  it should "be created with parameters" in {
    var coding = TransferCoding("CHUNKED; q=0.1")
    assert(coding.name == "chunked")
    assert(coding.isChunked)
    assert(coding.params("q").equals("0.1"))
    assert(coding.toString == "chunked; q=0.1")

    coding = TransferCoding("""GZIP; q=0.1; level="1 2 3" """)
    assert(coding.name == "gzip")
    assert(coding.isGzip)
    assert(coding.params("q").equals("0.1"))
    assert(coding.params("level").equals("1 2 3"))
    assert(coding.toString == "gzip; q=0.1; level=\"1 2 3\"" || coding.toString == "gzip; level=\"1 2 3\"; q=0.1")
  }

  it should "be destructured" in {
    val coding = TransferCoding("""Deflate; a=1; b=two; c="x y z" """)

    coding match {
      case TransferCoding(name, params) =>
        assert(name == coding.name)
        assert(params == coding.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](TransferCoding("chunked; q"))
    assertThrows[IllegalArgumentException](TransferCoding("chunked; q="))
    assertThrows[IllegalArgumentException](TransferCoding("chunked; =0.1"))
  }
}

