package scamper.types

import org.scalatest.FlatSpec

class LanguageRangeSpec extends FlatSpec {
  "LanguageRange" should "be created" in {
    var range = LanguageRange("en")
    assert(range.tag == "en")
    assert(range.weight == 1f)
    assert(range.toString == "en; q=1.0")

    range = LanguageRange("en; q=0.5")
    assert(range.tag == "en")
    assert(range.weight == 0.5f)
    assert(range.toString == "en; q=0.5")

    range = LanguageRange("en-US-1995; q=0.123456789")
    assert(range.tag == "en-US-1995")
    assert(range.weight == 0.123f)
    assert(range.toString == "en-US-1995; q=0.123")
  }

  it should "match LanguageTag" in {
    assert(LanguageRange("en").matches(LanguageTag("EN")))
    assert(LanguageRange("en; q=0.1").matches(LanguageTag("en-US")))
    assert(LanguageRange("en-US").matches(LanguageTag("en-US")))
    assert(LanguageRange("en-US; q=0.5").matches(LanguageTag("en-US-1995")))
  }

  it should "not match LanguageTag" in {
    assert(!LanguageRange("en-US; q=0.1").matches(LanguageTag("en")))
    assert(!LanguageRange("en-US-1995").matches(LanguageTag("en-US")))
    assert(!LanguageRange("en").matches(LanguageTag("fr")))
  }

  it should "be destructured" in {
    val range = LanguageRange("en-US-1995; q=0.6")

    range match {
      case LanguageRange(tag, weight) =>
        assert(tag == range.tag)
        assert(weight == range.weight)
    }
  }

  it should "not be created with invalid name" in {
    assertThrows[IllegalArgumentException](LanguageRange("en-US; q="))
    assertThrows[IllegalArgumentException](LanguageRange("1995-en-US; q=1.0"))
  }
}

