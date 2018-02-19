package scamper

import org.scalatest.FlatSpec

class MediaTypeSpec extends FlatSpec {
  "MediaType" should "be created without parameters" in {
    val contentType = MediaType("text/html")
    assert(contentType.primaryType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.isText)
    assert(contentType == MediaType(contentType.toString))
  }

  it should "be created with parameters" in {
    var contentType = MediaType("text/html; charset=iso-8859-1")
    assert(contentType.primaryType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "iso-8859-1")
    assert(contentType.isText)
    assert(contentType == MediaType(contentType.toString))

    contentType = MediaType("text", "html", "charset" -> "iso-8859-1")
    assert(contentType.primaryType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "iso-8859-1")
    assert(contentType.isText)
    assert(contentType == MediaType(contentType.toString))

    contentType = MediaType("text", "html", "charset" -> "utf-8", "not-a-charset" -> "iso 8859 1")
    assert(contentType.primaryType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "utf-8")
    assert(contentType.params("not-a-charset") == "iso 8859 1")
    assert(contentType.isText)
    assert(contentType == MediaType(contentType.toString))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](MediaType("(text)/html"))
    assertThrows[IllegalArgumentException](MediaType("text/(html)"))
    assertThrows[IllegalArgumentException](MediaType("text/html; charset"))
    assertThrows[IllegalArgumentException](MediaType("text/html; charset=iso 8859 1"))
  }
}

