package scamper

import bantam.nx.lang._
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => DateFormatter }
import org.scalatest.FlatSpec

class HeaderSpec extends FlatSpec {
  "Header" should "be created from formatted value" in {
    val header = Header("Content-Type: text/plain")
    assert(header.key == "Content-Type")
    assert(header.value == "text/plain")
    assert(header.toString == "Content-Type: text/plain")
  }

  it should "be created using long value" in {
    val header = Header("Content-Length", 80)
    assert(header.key == "Content-Length")
    assert(header.value == "80")
    assert(header.longValue == 80)
    assert(header.toString == "Content-Length: 80")
  }

  it should "be created using date value" in {
    val header = Header("If-Modified-Since", "2016-11-08T17:00:00-04:00".toOffsetDateTime)
    assert(header.key == "If-Modified-Since")
    assert(header.value == "Tue, 8 Nov 2016 21:00:00 GMT")
    assert(header.dateValue == OffsetDateTime.parse("Tue, 8 Nov 2016 21:00:00 GMT", DateFormatter))
    assert(header.toString == "If-Modified-Since: Tue, 8 Nov 2016 21:00:00 GMT")
  }

  it should "be created using folded value" in {
    val header = Header("Cookie", "user=guest,\r\n\tgroup=readonly")
    assert(header.key == "Cookie")
    assert(header.value == "user=guest,\r\n\tgroup=readonly")
  }

  it should "be destructured to its constituent parts" in {
    val header = Header("Content-Type: text/plain")
    
    header match {
      case Header(key, value) =>
        assert(header.key == "Content-Type")
        assert(header.value == "text/plain")
    }
  }

  it should "not be created from malformed value" in {
    assertThrows[IllegalArgumentException](Header("text/plain"))
    assertThrows[IllegalArgumentException](Header("Cookie", "user=guest,\r\ngroup=readonly"))
  }
}

