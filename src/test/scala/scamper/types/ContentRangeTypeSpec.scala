package scamper.types

import org.scalatest.FlatSpec
import scamper.types.ByteContentRange._

class ContentRangeTypeSpec extends FlatSpec {
  "ByteContentRange" should "be created with satisfied response" in {
    var range = ByteContentRange("bytes 8-15/1024")
    assert(range.unit == "bytes")
    assert(range.resp.asInstanceOf[Satisfied] == Satisfied(8, 15, Some(1024)))
    assert(range.toString == "bytes 8-15/1024")

    range = ByteContentRange("bytes 8-15/*")
    assert(range.unit == "bytes")
    assert(range.resp.asInstanceOf[Satisfied] == Satisfied(8, 15, None))
    assert(range.toString == "bytes 8-15/*")
  }

  it should "be created with unsatisfied response" in {
    val range = ByteContentRange("bytes */8192")
    assert(range.unit == "bytes")
    assert(range.resp.asInstanceOf[Unsatisfied] == Unsatisfied(8192))
    assert(range.toString == "bytes */8192")
  }
}

