package scamper

import org.scalatest.FlatSpec

class TCodingSpec extends FlatSpec {
  "TCoding" should "be created without parameters" in {
    var tcoding = TCoding("CHUNKED")
    assert(tcoding.name == "chunked")
    assert(tcoding.isChunked)
    assert(tcoding.rank == 1.0f)
    assert(tcoding.params.isEmpty)
    assert(tcoding.toString == "chunked")

    tcoding = TCoding("GZIP; q=0.7")
    assert(tcoding.name == "gzip")
    assert(tcoding.isGzip)
    assert(tcoding.rank == 0.7f)
    assert(tcoding.params.isEmpty)
    assert(tcoding.toString == "gzip; q=0.7")
  }

  it should "be created with parameters" in {
    var tcoding = TCoding("CHUNKED; x=abc")
    assert(tcoding.name == "chunked")
    assert(tcoding.isChunked)
    assert(tcoding.rank == 1.0f)
    assert(tcoding.params("x") == "abc")
    assert(tcoding.toString == "chunked; x=abc")

    tcoding = TCoding("""GZIP; q=0.1; level="1 2 3" """)
    assert(tcoding.name == "gzip")
    assert(tcoding.isGzip)
    assert(tcoding.rank == 0.1f)
    assert(tcoding.params("level") == "1 2 3")
    assert(tcoding.toString == "gzip; q=0.1; level=\"1 2 3\"")
  }

  it should "be destructured" in {
    val tcoding = TCoding("""Deflate; a=1; b=two; c="x y z" """)

    tcoding match {
      case TCoding(name, rank, params) =>
        assert(name == tcoding.name)
        assert(rank == tcoding.rank)
        assert(params == tcoding.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](TCoding("chunked; q"))
    assertThrows[IllegalArgumentException](TCoding("chunked; q="))
    assertThrows[IllegalArgumentException](TCoding("chunked; =0.1"))
  }
}

