/*
 * Copyright 2019 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scamper.cookies

import java.time.OffsetDateTime

import scamper.RequestMethod.Registry.GET
import scamper.ResponseStatus.Registry.Ok
import scamper.Uri

class CookiesSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "PlainCookie" should "be created from formatted value" in {
    val cookie = PlainCookie.parse("SID=31d4d96e407aad42")
    assert(cookie.name == "SID")
    assert(cookie.value == "31d4d96e407aad42")

    cookie match {
      case PlainCookie(name, value) =>
        assert(name == cookie.name)
        assert(value == cookie.value)
    }
  }

  it should "not be created from malformed value" in {
    assertThrows[IllegalArgumentException](PlainCookie.parse("S I D=31d4d96e407aad42"))
    assertThrows[IllegalArgumentException](PlainCookie.parse("SID=3 1d4d96e407aad4 2"))
  }

  "SetCookie" should "be created from formatted value" in {
    val cookie = SetCookie.parse("SID=31d4d96e407aad42; Path=/; Secure; HttpOnly; Expires=Wed, 09 Jun 2021 10:18:14 GMT")
    assert(cookie.name == "SID")
    assert(cookie.value == "31d4d96e407aad42")
    assert(cookie.path.contains("/"))
    assert(cookie.expires.contains(scamper.DateValue.parse("Wed, 09 Jun 2021 10:18:14 GMT")))
    assert(cookie.secure)

    cookie match {
      case SetCookie(name, value, domain, path, expires, maxAge, secure, httpOnly) =>
        assert(name == cookie.name)
        assert(value == cookie.value)
        assert(domain == cookie.domain)
        assert(path == cookie.path)
        assert(expires == cookie.expires)
        assert(maxAge == cookie.maxAge)
        assert(secure == cookie.secure)
        assert(httpOnly == cookie.httpOnly)
    }
  }

  it should "not be created from malformed value" in {
    assertThrows[IllegalArgumentException](SetCookie.parse("S I D=31d4d96e407aad42"))
    assertThrows[IllegalArgumentException](SetCookie.parse("SID=3\\1d4d96e407aad42"))
  }

  "HttpRequest" should "be created with cookies" in {
    val req = GET(Uri("/")).withCookies(PlainCookie("a", "123"), PlainCookie("b", "xyz")).withCookie(PlainCookie("c", "XYZ"))
    assert(req.getHeaderValue("Cookie").contains("a=123; b=xyz; c=XYZ"))
    assert(req.getCookieValue("a").contains("123"))
    assert(req.getCookieValue("b").contains("xyz"))
    assert(req.getCookieValue("c").contains("XYZ"))
  }

  it should "be created without cookies" in {
    val req = GET(Uri("/")).withCookies(PlainCookie("a", "123"), PlainCookie("b", "xyz"), PlainCookie("c", "XYZ"))
    assert(req.withCookies().getHeaderValue("Cookie").isEmpty)
    assert(req.removeCookies("a", "c").cookies == Seq(PlainCookie("b", "xyz")))
  }

  "HttpResponse" should "be created with cookies" in {
    val res = Ok().withCookies(SetCookie("a", "123"), SetCookie("b", "xyz")).withCookie(SetCookie("c", "XYZ"))
    assert(res.getHeaderValue("Set-Cookie").contains("a=123"))
    assert(res.getHeaderValues("Set-Cookie").sameElements(Seq("a=123", "b=xyz", "c=XYZ")))
    assert(res.getCookieValue("a").contains("123"))
    assert(res.getCookieValue("b").contains("xyz"))
    assert(res.getCookieValue("c").contains("XYZ"))
  }

  it should "be created without cookies" in {
    val res = Ok().withCookies(SetCookie("a", "123"), SetCookie("b", "xyz"), SetCookie("c", "XYZ"))
    assert(res.withCookies().getHeaderValue("Set-Cookie").isEmpty)
    assert(res.removeCookies("a", "c").cookies == Seq(SetCookie("b", "xyz")))
  }
}
