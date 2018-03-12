package scamper.types

import org.scalatest.FlatSpec
import PragmaDirectives._

class PragmaDirectiveSpec extends FlatSpec {
  "PragmaDirective" should "be created" in {
    val directive = PragmaDirective("No-Cache")
    assert(directive.name == "no-cache")
    assert(!directive.value.isDefined)
    assert(directive.toString == "no-cache")
    assert(directive == `no-cache`)

    val ext = PragmaDirective("community", Some("In Library"))
    assert(ext.name == "community")
    assert(ext.value.contains("In Library"))
    assert(ext.toString == "community=\"In Library\"")
    assert(ext == PragmaDirective("Community", Some("In Library")))
  }

  it should "be parsed" in {
    assert(PragmaDirective.parse("no-cache") == `no-cache`)
    assert(PragmaDirective.parse("community = \"home\"") == PragmaDirective("Community", Some("home")))
    assert(PragmaDirective.parseAll("no-cache, community = \"home\"") == Seq(`no-cache`, PragmaDirective("Community", Some("home"))))
  }

  it should "be destructured" in {
    `no-cache` match {
      case PragmaDirective(name, None) => assert(name == "no-cache")
    }

    `no-cache` match { case `no-cache` => }

    PragmaDirective("max-age", Some("60")) match {
      case PragmaDirective(name, Some(value)) => assert(name == "max-age" && value == "60")
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](PragmaDirective("no/cache"))
    assertThrows[IllegalArgumentException](PragmaDirective("no-cache ="))
  }
}

