package scamper.types

import org.scalatest.FlatSpec

class ContentDispositionTypeSpec extends FlatSpec {
  "ContentDispositionType" should "be created without parameters" in {
    var disposition = ContentDispositionType("INLINE")
    assert(disposition.name == "inline")
    assert(disposition.isInline)
    assert(!disposition.isAttachment)
    assert(disposition.params.isEmpty)
    assert(disposition.toString == "inline")

    disposition = ContentDispositionType("Attachment")
    assert(disposition.name == "attachment")
    assert(disposition.isAttachment)
    assert(!disposition.isInline)
    assert(disposition.params.isEmpty)
    assert(disposition.toString == "attachment")
  }

  it should "be created with parameters" in {
    var disposition = ContentDispositionType("Inline; Filename=\"a text file.txt\"")
    assert(disposition.name == "inline")
    assert(disposition.isInline)
    assert(!disposition.isAttachment)
    assert(disposition.params.size == 1)
    assert(disposition.params.get("filename").contains("a text file.txt"))
    assert(disposition.toString == "inline; filename=\"a text file.txt\"")

    disposition = ContentDispositionType("ATTACHMENT; FILENAME*=\"example.txt\"")
    assert(disposition.name == "attachment")
    assert(disposition.isAttachment)
    assert(!disposition.isInline)
    assert(disposition.params.size == 1)
    assert(disposition.params.get("filename*").contains("example.txt"))
    assert(disposition.toString == "attachment; filename*=example.txt")
  }

  it should "be destructured" in {
    val disposition = ContentDispositionType("""extended-type; a=1; b="t w o"; c=trois""")

    disposition match {
      case ContentDispositionType(name, params) =>
        assert(name == disposition.name)
        assert(params == disposition.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](ContentDispositionType("inline; filename"))
    assertThrows[IllegalArgumentException](ContentDispositionType("inline; filename="))
    assertThrows[IllegalArgumentException](ContentDispositionType("inline; =0.1"))
  }
}

