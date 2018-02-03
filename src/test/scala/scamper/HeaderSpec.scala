package scamper

import bantam.nx.lang._
import org.scalatest.FlatSpec

class HeaderSpec extends FlatSpec {
  "Header" should "be created using formatted value" in {
    val header = Header("Content-Type: text/plain")

    assert(header.key == "Content-Type")
    assert(header.value == "text/plain")
    assert(header == Header(header.toString))
  }

  it should "be created using long value" in {
    val header = Header("Content-Length", 80)

    assert(header.key == "Content-Length")
    assert(header.value == "80")
    assert(header.longValue == 80)
    assert(header == Header(header.toString))
  }

  it should "be created using date value" in {
    val formatter = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
    val date = "2016-11-08T09:00:00+04:00".toOffsetDateTime
    val header = Header("If-Modified-Since", date)

    assert(header.key == "If-Modified-Since")
    assert(header.value == formatter.format(date))
    assert(header.dateValue == date)
    assert(header == Header(header.toString))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](Header("Content-Type"))
    assertThrows[IllegalArgumentException](Header("text/plain"))
  }
}

