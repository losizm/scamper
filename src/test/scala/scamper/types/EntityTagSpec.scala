package scamper.types

import org.scalatest.FlatSpec

class EntityTagSpec extends FlatSpec {
  "EntityTag" should "be created" in {
    var tag = EntityTag("\"abc\"")
    assert(tag.opaque == "\"abc\"")
    assert(!tag.weak)
    assert(tag.toString == "\"abc\"")
    assert(tag == EntityTag("\"abc\"", false))

    tag = EntityTag("W/\"xyz\"")
    assert(tag.opaque == "\"xyz\"")
    assert(tag.weak)
    assert(tag.toString == "W/\"xyz\"")
    assert(tag == EntityTag("xyz", true))
  }

  it should "be destructured" in {
    EntityTag("\"abc\"") match {
      case EntityTag(opaque, weak) => assert(opaque == "\"abc\"" && !weak)
    }

    EntityTag("W/\"xyz\"") match {
      case EntityTag(opaque, weak) => assert(opaque == "\"xyz\"" && weak)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](EntityTag("w/\"abc\""))
    assertThrows[IllegalArgumentException](EntityTag("abc"))
    assertThrows[IllegalArgumentException](EntityTag("\"abc\"xyz\""))
  }
}

