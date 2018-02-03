package scamper

import org.scalatest.FlatSpec

class ContentTypeSpec extends FlatSpec {
  "ContentType" should "be created without parameters" in {
    val contentType = ContentType("text/html")
    assert(contentType.primaryType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.isText)
    assert(contentType == ContentType(contentType.toString))
  }

  it should "be created with parameters" in {
    var contentType = ContentType("text/html; charset=iso-8859-1")
    assert(contentType.primaryType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.parameters("charset") == "iso-8859-1")
    assert(contentType.isText)
    assert(contentType == ContentType(contentType.toString))

    contentType = ContentType("text", "html", "charset" -> "iso-8859-1")
    assert(contentType.primaryType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.parameters("charset") == "iso-8859-1")
    assert(contentType.isText)
    assert(contentType == ContentType(contentType.toString))

    contentType = ContentType("text", "html", "charset" -> "utf-8", "not-a-charset" -> "iso 8859 1")
    assert(contentType.primaryType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.parameters("charset") == "utf-8")
    assert(contentType.parameters("not-a-charset") == "iso 8859 1")
    assert(contentType.isText)
    assert(contentType == ContentType(contentType.toString))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](ContentType("(text)/html"))
    assertThrows[IllegalArgumentException](ContentType("text/(html)"))
    assertThrows[IllegalArgumentException](ContentType("text/html; charset"))
    assertThrows[IllegalArgumentException](ContentType("text/html; charset=iso 8859 1"))
  }
}

