/*
 * Copyright 2021 Carlos Conyers
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
package scamper
package http
package client

import scala.language.implicitConversions

import scamper.http.cookies.*
import scamper.http.server.TestServer

import ResponseStatus.Registry.Ok

class HttpClientCookieSpec extends org.scalatest.flatspec.AnyFlatSpec with TestServer:
  it should "validate cookies" in testClient(false)

  it should "validate cookies with SSL/TLS" in testClient(true)

  private def testClient(secure: Boolean) = withServer(secure) { implicit server =>
    given client: HttpClient =
      HttpClient
        .settings()
        .trust(Resources.truststore)
        .outgoing(doCookieCheck(secure)(_))
        .cookies()
        .create()

    info("send request to /cookies/foo/1")
    client.get(s"$serverUri/cookies/foo/1") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").contains("foo_public_value"))
      assert(res.getCookieValue("bar_public").isEmpty)
      assert(res.getCookieValue("baz_public").isEmpty)

      secure match
        case true  => assert(res.getCookieValue("foo_secure").contains("foo_secure_value"))
        case false => assert(res.getCookieValue("foo_secure").isEmpty)

      assert(res.getCookieValue("bar_secure").isEmpty)
      assert(res.getCookieValue("baz_secure").isEmpty)
    }

    info("send request to /cookies/foo/2")
    client.get(s"$serverUri/cookies/foo/2") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").isEmpty)
      assert(res.getCookieValue("bar_public").isEmpty)
      assert(res.getCookieValue("baz_public").isEmpty)

      assert(res.getCookieValue("foo_secure").isEmpty)
      assert(res.getCookieValue("bar_secure").isEmpty)
      assert(res.getCookieValue("baz_secure").isEmpty)
    }

    info("send request to /cookies/foo/bar/1")
    client.get(s"$serverUri/cookies/foo/bar/1") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").isEmpty)
      assert(res.getCookieValue("bar_public").contains("bar_public_value"))
      assert(res.getCookieValue("baz_public").isEmpty)

      assert(res.getCookieValue("foo_secure").isEmpty)

      secure match
        case true  => assert(res.getCookieValue("bar_secure").contains("bar_secure_value"))
        case false => assert(res.getCookieValue("bar_secure").isEmpty)

      assert(res.getCookieValue("baz_secure").isEmpty)
    }

    info("send request to /cookies/foo/bar/2")
    client.get(s"$serverUri/cookies/foo/bar/2") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").isEmpty)
      assert(res.getCookieValue("bar_public").isEmpty)
      assert(res.getCookieValue("baz_public").isEmpty)

      assert(res.getCookieValue("foo_secure").isEmpty)
      assert(res.getCookieValue("bar_secure").isEmpty)
      assert(res.getCookieValue("baz_secure").isEmpty)
    }

    info("send request to /cookies/foo/bar/baz/1")
    client.get(s"$serverUri/cookies/foo/bar/baz/1") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").isEmpty)
      assert(res.getCookieValue("bar_public").isEmpty)
      assert(res.getCookieValue("baz_public").contains("baz_public_value"))

      assert(res.getCookieValue("foo_secure").isEmpty)
      assert(res.getCookieValue("bar_secure").isEmpty)

      secure match
        case true  => assert(res.getCookieValue("baz_secure").contains("baz_secure_value"))
        case false => assert(res.getCookieValue("baz_secure").isEmpty)
    }

    info("send request to /cookies/foo/bar/baz/2")
    client.get(s"$serverUri/cookies/foo/bar/baz/2") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").isEmpty)
      assert(res.getCookieValue("bar_public").isEmpty)
      assert(res.getCookieValue("baz_public").isEmpty)

      assert(res.getCookieValue("foo_secure").isEmpty)
      assert(res.getCookieValue("bar_secure").isEmpty)
      assert(res.getCookieValue("baz_secure").isEmpty)
    }

    info("send request to /cookies/foo/bar/baz/qux/1")
    client.get(s"$serverUri/cookies/foo/bar/baz/qux/1") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").isEmpty)
      assert(res.getCookieValue("bar_public").isEmpty)
      assert(res.getCookieValue("baz_public").isEmpty)
      assert(res.getCookieValue("foo_secure").isEmpty)
      assert(res.getCookieValue("bar_secure").isEmpty)
      assert(res.getCookieValue("baz_secure").isEmpty)
    }

    // Clear all cookies
    client.cookies.clear()

    info("send request to /cookies/foo/bar/baz/qux/2")
    client.get(s"$serverUri/cookies/foo/bar/baz/qux/2") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").contains("foo_public_value"))
      assert(res.getCookieValue("bar_public").contains("bar_public_value"))
      assert(res.getCookieValue("baz_public").contains("baz_public_value"))

      secure match
        case true  => assert(res.getCookieValue("foo_secure").contains("foo_secure_value"))
        case false => assert(res.getCookieValue("foo_secure").isEmpty)

      secure match
        case true  => assert(res.getCookieValue("bar_secure").contains("bar_secure_value"))
        case false => assert(res.getCookieValue("bar_secure").isEmpty)

      secure match
        case true  => assert(res.getCookieValue("baz_secure").contains("baz_secure_value"))
        case false => assert(res.getCookieValue("baz_secure").isEmpty)
    }

    info("send request to /cookies/foo/bar/baz/qux/3")
    client.get(s"$serverUri/cookies/foo/bar/baz/qux/3") { res =>
      info("check response status")
      assert(res.status == Ok)
      assert(res.getCookieValue("foo_public").isEmpty)
      assert(res.getCookieValue("bar_public").isEmpty)
      assert(res.getCookieValue("baz_public").isEmpty)
      assert(res.getCookieValue("foo_secure").isEmpty)
      assert(res.getCookieValue("bar_secure").isEmpty)
      assert(res.getCookieValue("baz_secure").isEmpty)
    }
  }

  private def doCookieCheck(secure: Boolean)(req: HttpRequest): HttpRequest =
    info(s"check request cookies")
    assert(req.cookies.sortBy(_.name) == getCookies(req.path, secure).sortBy(_.name))
    req

  private def getCookies(path: String, secure: Boolean): Seq[PlainCookie] =
    path match
      case "/cookies/foo/1" =>
        Nil

      case "/cookies/foo/2" =>
        PlainCookie("foo_public", "foo_public_value") +:
          (if secure then Seq(PlainCookie("foo_secure", "foo_secure_value")) else Nil)

      case "/cookies/foo/bar/1" =>
        getCookies("/cookies/foo/2", secure)

      case "/cookies/foo/bar/2" =>
        getCookies("/cookies/foo/bar/1", secure) ++
          (PlainCookie("bar_public", "bar_public_value") +:
            (if secure then Seq(PlainCookie("bar_secure", "bar_secure_value")) else Nil))

      case "/cookies/foo/bar/baz/1" =>
        getCookies("/cookies/foo/bar/2", secure)

      case "/cookies/foo/bar/baz/2" =>
        getCookies("/cookies/foo/bar/baz/1", secure) ++
          (PlainCookie("baz_public", "baz_public_value") +:
            (if secure then Seq(PlainCookie("baz_secure", "baz_secure_value")) else Nil))

      case "/cookies/foo/bar/baz/qux/1" =>
        getCookies("/cookies/foo/bar/baz/2", secure)

      case "/cookies/foo/bar/baz/qux/2" =>
        Nil

      case "/cookies/foo/bar/baz/qux/3" =>
        getCookies("/cookies/foo/bar/baz/qux/1", secure)

      case _ =>
        throw IllegalArgumentException(s"Unexpected test path: $path")
