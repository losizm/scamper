package scamper.types

import org.scalatest.FlatSpec
import CacheDirectives._

class CacheDirectiveSpec extends FlatSpec {
  "CacheDirective" should "be created" in {
    var directive = CacheDirective("No-Cache")
    assert(directive.name == "no-cache")
    assert(!directive.value.isDefined)
    assert(directive.toString == "no-cache")
    assert(directive == `no-cache`)

    directive = CacheDirective("MAX-AGE", Some("6000"))
    assert(directive.name == "max-age")
    assert(directive.value.contains("6000"))
    assert(directive.toString == "max-age=6000")
    assert(directive == `max-age`(6000))

    val maxAge = `max-age`(3000)
    assert(maxAge.name == "max-age")
    assert(maxAge.value.contains("3000"))
    assert(maxAge.deltaSeconds == 3000)
    assert(maxAge.toString == "max-age=3000")
    assert(maxAge == CacheDirective("max-age", Some("3000")))

    val ext = CacheDirective("community", Some("In Library"))
    assert(ext.name == "community")
    assert(ext.value.contains("In Library"))
    assert(ext.toString == "community=\"In Library\"")
    assert(ext == CacheDirective("Community", Some("In Library")))
  }

  it should "be parsed" in {
    assert(CacheDirective.parse("no-store") == `no-store`)
    assert(CacheDirective.parse("min-fresh = 90") == `min-fresh`(90))
    assert(CacheDirective.parseAll("min-fresh = 90") == Seq(`min-fresh`(90)))
    assert(CacheDirective.parseAll("no-store, max-age=120") == Seq(`no-store`, `max-age`(120)))
  }

  it should "be destructured" in {
    val sMaxage = `s-maxage`(60)

    sMaxage match {
      case CacheDirective(name, Some(value)) =>
        assert(name == "s-maxage")
        assert(value == "60")
    }

    sMaxage match {
      case `s-maxage`(secs) => assert(secs == 60)
    }

    val mustRevalidate = CacheDirective("Must-Revalidate")

    mustRevalidate match {
      case CacheDirective(name, value) =>
        assert(name == "must-revalidate")
        assert(!value.isDefined)
    }

    mustRevalidate match {
      case `must-revalidate` =>
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](CacheDirective("Basic /"))
    assertThrows[IllegalArgumentException](CacheDirective("Basic ="))
    assertThrows[IllegalArgumentException](CacheDirective("Basic =secret"))
  }
}

