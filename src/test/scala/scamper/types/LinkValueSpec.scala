package scamper.types

import org.scalatest.FlatSpec

class LinkValueSpec extends FlatSpec {
  "LinkValue" should "be created" in {
    var link = LinkValue("/assets/icon.png")
    assert(link.ref == "/assets/icon.png")
    assert(link.params.isEmpty)
    assert(link.toString == "</assets/icon.png>")
    assert(link == LinkValue("/assets/icon.png"))

    link = LinkValue("/assets/large-icon.png", "size" -> Some("64x64"))
    assert(link.ref == "/assets/large-icon.png")
    assert(link.params("size") == Some("64x64"))
    assert(link.toString == "</assets/large-icon.png>; size=64x64")
    assert(link == LinkValue("/assets/large-icon.png", "size" -> Some("64x64")))
  }

  it should "be parsed" in {
    assert(LinkValue.parse("</assets/icon.png>") == LinkValue("/assets/icon.png"))
    assert(LinkValue.parse("</assets/large-icon.png>;size=64x64") == LinkValue("/assets/large-icon.png", "size" -> Some("64x64")))
    assert {
      LinkValue.parseAll("</assets/icon.png>,</assets/large-icon.png>;size=64x64") ==
        Seq(LinkValue("/assets/icon.png"),LinkValue("/assets/large-icon.png", "size" -> Some("64x64")))
    }
  }

  it should "be destructured" in {
    LinkValue.parse("</assets/icon.png>") match {
      case LinkValue(ref, params) => assert(ref == "/assets/icon.png" && params.isEmpty)
    }

    LinkValue.parse("</assets/large-icon.png>;size=64x64") match {
      case LinkValue(ref, params) =>
        assert(ref == "/assets/large-icon.png")
        assert(params("size") == Some("64x64"))
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](LinkValue.parse("/assets/icon.png"))
    assertThrows[IllegalArgumentException](LinkValue.parse("/assets/icon.png>; size=64x64"))
  }
}

