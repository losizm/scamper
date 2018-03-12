package scamper.types

import org.scalatest.FlatSpec

class ProtocolSpec extends FlatSpec {
  "Protocol" should "be created" in {
    var protocol = Protocol("HTTP/2.0")
    assert(protocol.name == "HTTP")
    assert(protocol.version.contains("2.0"))
    assert(protocol.toString == "HTTP/2.0")
    assert(protocol == Protocol("HTTP", Some("2.0")))

    protocol = Protocol("SHTTP/1.3")
    assert(protocol.name == "SHTTP")
    assert(protocol.version.contains("1.3"))
    assert(protocol.toString == "SHTTP/1.3")
    assert(protocol == Protocol("SHTTP", Some("1.3")))

    protocol = Protocol("IRC")
    assert(protocol.name == "IRC")
    assert(protocol.version == None)
    assert(protocol.toString == "IRC")
    assert(protocol == Protocol("IRC", None))
  }

  it should "be destructured" in {
    Protocol("HTTP/2.0") match {
      case Protocol(name, Some(version)) => assert(name == "HTTP" && version == "2.0")
    }

    Protocol("SHTTP/1.3") match {
      case Protocol(name, Some(version)) => assert(name == "SHTTP" && version == "1.3")
    }

    Protocol("IRC") match {
      case Protocol(name, None) => assert(name == "IRC")
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Protocol("HTTP / 2.0"))
    assertThrows[IllegalArgumentException](Protocol("HTTP/"))
  }
}

