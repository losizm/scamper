package scamper.types

import org.scalatest.FlatSpec

class TransferCodingRangeSpec extends FlatSpec {
  "TransferCodingRange" should "be created without parameters" in {
    var tcoding = TransferCodingRange("CHUNKED")
    assert(tcoding.name == "chunked")
    assert(tcoding.isChunked)
    assert(tcoding.rank == 1.0f)
    assert(tcoding.params.isEmpty)
    assert(tcoding.toString == "chunked; q=1.0")

    tcoding = TransferCodingRange("GZIP; q=0.7")
    assert(tcoding.name == "gzip")
    assert(tcoding.isGzip)
    assert(tcoding.rank == 0.7f)
    assert(tcoding.params.isEmpty)
    assert(tcoding.toString == "gzip; q=0.7")
  }

  it should "be created with parameters" in {
    var tcoding = TransferCodingRange("CHUNKED; x=abc")
    assert(tcoding.name == "chunked")
    assert(tcoding.isChunked)
    assert(tcoding.rank == 1.0f)
    assert(tcoding.params("x") == "abc")
    assert(tcoding.toString == "chunked; q=1.0; x=abc")

    tcoding = TransferCodingRange("""GZIP; q=0.1; level="1 2 3" """)
    assert(tcoding.name == "gzip")
    assert(tcoding.isGzip)
    assert(tcoding.rank == 0.1f)
    assert(tcoding.params("level") == "1 2 3")
    assert(tcoding.toString == "gzip; q=0.1; level=\"1 2 3\"")
  }

  it should "match TransferCoding" in {
    assert(TransferCodingRange("chunked; q=1.0; x=0").matches(TransferCoding("chunked; x=0; y=1")))
    assert(TransferCodingRange("chunked; q=1.0; x=0; y=1").matches(TransferCoding("chunked; x=0; y=1")))
    assert(TransferCodingRange("gzip").matches(TransferCoding("gzip")))
    assert(TransferCodingRange("gzip").matches(TransferCoding("gzip; x=0")))
  }

  it should "not match TransferCoding" in {
    assert(!TransferCodingRange("chunked; q=1.0; x=0").matches(TransferCoding("chunked; y=1")))
    assert(!TransferCodingRange("chunked; q=1.0; x=0; y=1").matches(TransferCoding("chunked; x=0")))
    assert(!TransferCodingRange("gzip; y=1").matches(TransferCoding("gzip; x=0")))
    assert(!TransferCodingRange("gzip; y=1").matches(TransferCoding("gzip")))
  }

  it should "be destructured" in {
    val tcoding = TransferCodingRange("""Deflate; a=1; b=two; c="x y z" """)

    tcoding match {
      case TransferCodingRange(name, rank, params) =>
        assert(name == tcoding.name)
        assert(rank == tcoding.rank)
        assert(params == tcoding.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](TransferCodingRange("chunked; q"))
    assertThrows[IllegalArgumentException](TransferCodingRange("chunked; q="))
    assertThrows[IllegalArgumentException](TransferCodingRange("chunked; =0.1"))
  }
}

