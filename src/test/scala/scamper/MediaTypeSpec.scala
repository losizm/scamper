package scamper

import org.scalatest.FlatSpec

class MediaTypeSpec extends FlatSpec {
  "MediaType" should "be created without parameters" in {
    val contentType = MediaType("text/html")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.isText)
    assert(contentType.toString == "text/html")
  }

  it should "be created with parameters" in {
    var contentType = MediaType("text/html; charset=iso-8859-1")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "iso-8859-1")
    assert(contentType.isText)
    assert(contentType.toString == "text/html; charset=iso-8859-1")

    contentType = MediaType("text", "html", "charset" -> "iso-8859-1")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "iso-8859-1")
    assert(contentType.isText)
    assert(contentType.toString == "text/html; charset=iso-8859-1")

    contentType = MediaType("text", "html", "charset" -> "utf-8", "not-a-charset" -> "iso 8859 1")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "utf-8")
    assert(contentType.params("not-a-charset") == "iso 8859 1")
    assert(contentType.isText)
    assert(contentType.toString == "text/html; charset=utf-8; not-a-charset=\"iso 8859 1\"")
  }

  it should "be destructured to its constituent parts" in {
    val contentType = MediaType("text/html; charset=iso-8859-1")

    contentType match {
      case MediaType(mainType, subtype, params) =>
        assert(mainType == contentType.mainType)
        assert(subtype == contentType.subtype)
        assert(params == contentType.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](MediaType("(text)/html"))
    assertThrows[IllegalArgumentException](MediaType("text/(html)"))
    assertThrows[IllegalArgumentException](MediaType("text/html; charset"))
    assertThrows[IllegalArgumentException](MediaType("text/html; charset=iso 8859 1"))
  }
}

