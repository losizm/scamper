/*
 * Copyright 2017-2020 Carlos Conyers
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

import java.time.Instant

import scamper.Implicits.stringToUri
import scamper.Uri

class CookieStoreSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "get cookies with matching host" in {
    val a = SetCookie("a", "1")
    val b = SetCookie("b", "2")
    val c = SetCookie("c", "3")

    val cookieStore = CookieStore()
      .put("https://abc.com", a)
      .put("https://ht.abc.com", b)
      .put("https://us.abc.com", c)
    assert { cookieStore.size == 3 }

    val cookies = cookieStore.get("https://ht.abc.com/a/b/c")
    assert { matches(cookies, b) }
  }

  it should "get cookies with matching domain" in {
    val a = SetCookie("a", "1", domain = Some("abc.com"))
    val b = SetCookie("b", "2", domain = Some("ht.abc.com"))
    val c = SetCookie("c", "3", domain = Some("us.abc.com"))

    val cookieStore = CookieStore()
      .put("https://abc.com", a)
      .put("https://ht.abc.com", b)
      .put("https://us.abc.com", c)
    assert { cookieStore.size == 3 }

    val cookies = cookieStore.get("https://ht.abc.com/a/b/c")
    assert { matches(cookies, a, b) }
  }

  it should "get cookies with matching path" in {
    val a = SetCookie("a", "1", domain = Some("abc.com"), path = Some("/a"))
    val b = SetCookie("b", "2", domain = Some("abc.com"), path = Some("/a/b"))
    val c = SetCookie("c", "3", domain = Some("abc.com"), path = Some("/c"))

    val cookieStore = CookieStore()
      .put("https://abc.com", a)
      .put("https://ht.abc.com", b)
      .put("https://us.abc.com", c)
    assert { cookieStore.size == 3 }

    val cookies = cookieStore.get("https://ht.abc.com/a/b/c")
    assert { matches(cookies, a, b) }
  }

  it should "get cookies that are not expired" in {
    val a = SetCookie("a", "1", expires = None)
    val b = SetCookie("b", "2", expires = Some(Instant.now().plusSeconds(60)))
    val c = SetCookie("c", "3", expires = Some(Instant.now().minusSeconds(60)))
    val d = SetCookie("d", "4", maxAge = None)
    val e = SetCookie("e", "5", maxAge = Some(60))
    val f = SetCookie("f", "6", maxAge = Some(-60))

    val cookieStore = CookieStore().put("https://abc.com", a, b, c, d, e, f)
    assert { cookieStore.size == 6 }

    val cookies = cookieStore.get("https://abc.com/a/b/c")
    assert { matches(cookies, a, b, d, e) }
  }

  it should "get cookies that are not secure" in {
    val a = SetCookie("a", "1", secure = false)
    val b = SetCookie("b", "2", secure = true)

    val cookieStore = CookieStore().put("https://abc.com", a, b)
    assert { cookieStore.size == 2 }

    val cookies1 = cookieStore.get("http://abc.com/a/b/c")
    assert { matches(cookies1, a) }

    val cookies2 = cookieStore.get("https://abc.com/a/b/c")
    assert { matches(cookies2, a, b) }
  }

  it should "get cookies that are ordered by longest path" in {
    val a = SetCookie("a", "1", path = None)
    val b = SetCookie("b", "2", path = Some("/a"))
    val c = SetCookie("c", "3", path = Some("/a/b/c"))
    val d = SetCookie("d", "4", path = Some("/a/b"))
    val e = SetCookie("e", "5", path = Some("/a/b/c/d/e/f"))
    val f = SetCookie("f", "6", path = Some("/a/b/c/d"))
    val g = SetCookie("g", "7", path = Some("/a/b/c/d/e"))

    val cookieStore = CookieStore().put("https://abc.com", a, b, c, d, e, f, g)
    assert { cookieStore.size == 7 }

    val cookies = cookieStore.get("http://abc.com/a/b/c/d/e/f")
    assert { cookies == Seq(e, g, f, c, d, b, a).map(_.toPlainCookie) }
  }

  it should "replace existing cookie" in {
    val a1 = SetCookie("a", "1", domain = Some("abc.com"), path = Some("/a/b/c"), httpOnly = true)
    val a2 = SetCookie("a", "2", domain = Some("abc.com"), path = Some("/a/b/c"), maxAge = Some(60), secure = true)

    val cookieStore = CookieStore().put("https://abc.com", a1)
    assert { cookieStore.size == 1 }

    val oldCookie = cookieStore.list.head
    assert { oldCookie.name == "a" }
    assert { oldCookie.value == "1" }
    assert { oldCookie.domain == "abc.com" }
    assert { oldCookie.path == "/a/b/c" }
    assert { !oldCookie.secureOnly }
    assert { oldCookie.httpOnly }
    assert { !oldCookie.persistent }

    cookieStore.put("https://abc.com", a2)
    assert { cookieStore.size == 1 }

    val newCookie = cookieStore.list.head
    assert { newCookie.name == "a" }
    assert { newCookie.value == "2" }
    assert { newCookie.domain == "abc.com" }
    assert { newCookie.path == "/a/b/c" }
    assert { newCookie.secureOnly }
    assert { !newCookie.httpOnly }
    assert { newCookie.persistent }
  }

  it should "list and clear cookies" in {
    val a = SetCookie("a", "1")
    val b = SetCookie("b", "2", maxAge = Some(60))
    val c = SetCookie("c", "3", maxAge = Some(-60))
    val d = SetCookie("d", "4", expires = Some(Instant.now().plusSeconds(60)))
    val e = SetCookie("e", "5", expires = Some(Instant.now().minusSeconds(60)))

    val cookieStore = CookieStore().put("https://abc.com", a, b, c, d, e)
    assert { cookieStore.size == 5 }
    assert { matches(cookieStore.list.map(_.toPlainCookie), a, b, c, d, e) }

    cookieStore.clear(true)
    assert { cookieStore.size == 3 }

    val cookies = cookieStore.get("https://abc.com")
    assert { matches(cookies, a, b, d) }

    cookieStore.clear()
    assert { cookieStore.size == 0 }
  }

  private def matches(plain: Seq[PlainCookie], set: SetCookie*): Boolean =
    set.size == plain.size &&
      set.forall(cookie => plain.contains(cookie.toPlainCookie))
}
