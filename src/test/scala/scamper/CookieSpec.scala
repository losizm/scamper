package scamper

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.{ RFC_1123_DATE_TIME => expiresFormatter }

import org.scalatest.FlatSpec

class CookieSpec extends FlatSpec {
  "PlainCookie" should "be created using formatted value" in {
    val cookie = PlainCookie("SID=31d4d96e407aad42")
    assert(cookie.name == "SID")
    assert(cookie.value == "31d4d96e407aad42")
    assert(cookie == PlainCookie(cookie.toString))
  }

  "SetCookie" should "be created using formatted value" in {
    val cookie = SetCookie("SID=31d4d96e407aad42; Path=/; Secure; HttpOnly; Expires=Wed, 09 Jun 2021 10:18:14 GMT")
    assert(cookie.name == "SID")
    assert(cookie.value == "31d4d96e407aad42")
    assert(cookie.path.contains("/"))
    assert(cookie.expires.contains(OffsetDateTime.parse("Wed, 09 Jun 2021 10:18:14 GMT", expiresFormatter)))
    assert(cookie.secure)
    assert(cookie == SetCookie(cookie.toString))
  }
}

