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
package scamper.server

import scamper.Implicits.stringToUri
import scamper.{ HttpRequest, HttpResponse }
import scamper.RequestMethod.Registry.{ Delete, Get, Post, Put }
import scamper.ResponseStatus.Registry.Ok

import Implicits.ServerHttpRequestType

class TargetedRequestHandlerSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "TargetedRequestHandler" should "respond to request" in {
    val handler = TargetedRequestHandler(req => Ok(), "/", None)
    assert { handler(Get("/")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(Post("/")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(Put("/")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(Delete("/")).asInstanceOf[HttpResponse].status == Ok }
  }

  it should "respond to request with certain request method" in {
    val handler = TargetedRequestHandler(req => Ok(), "/", Some(Put))
    assert { handler(Get("/")).isInstanceOf[HttpRequest] }
    assert { handler(Post("/")).isInstanceOf[HttpRequest] }
    assert { handler(Put("/")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(Delete("/")).isInstanceOf[HttpRequest] }
  }

  it should "respond to request with certain path" in {
    val handler = TargetedRequestHandler(req => Ok(), "/a/b/c", None)
    assert { handler(Get("http://localhost:8080//a//b/../../a/b////c")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(Post("/a/.//b/c")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(Put("/a/b/c")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(Delete("/a/b/c")).asInstanceOf[HttpResponse].status == Ok }
  }

  it should "respond to request with certain path and request method" in {
    val handler = TargetedRequestHandler(req => Ok(), "/a/b/c", Some(Post))
    assert { handler(Get("/a/b/c")).isInstanceOf[HttpRequest] }
    assert { handler(Post("/a/b/c")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(Put("/a/b/c")).isInstanceOf[HttpRequest] }
    assert { handler(Delete("/a/b/c")).isInstanceOf[HttpRequest] }
  }

  it should "have access to request parameters" in {
    val h1 = TargetedRequestHandler(
      { req =>
        assert(req.params.getString("a") == "One")
        assert(req.params.getInt("b") == 200)
        assert(req.params.getLong("c") == 3000)
        Ok()
      },
      "/A/B/C/:a/:b/:c/d",
      None
    )

    assert { h1(Get("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h1(Post("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h1(Put("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h1(Delete("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }

    assert { h1(Get("/a/B/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h1(Post("/A/b/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h1(Put("/A/B/c/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h1(Delete("/a/b/c/One/200/3000/d")).isInstanceOf[HttpRequest] }

    val h2 = TargetedRequestHandler(
      { req =>
        assert(req.params.getString("abc") == "One/200/3000/d")
        Ok()
      },
      "/A/B/C/*abc",
      None
    )

    assert { h2(Get("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h2(Post("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h2(Put("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h2(Delete("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }

    assert { h2(Get("/a/B/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h2(Post("/A/b/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h2(Put("/A/B/c/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h2(Delete("/a/b/c/One/200/3000/d")).isInstanceOf[HttpRequest] }

    val h3 = TargetedRequestHandler(
      { req =>
        assert(req.params.getString("a") == "One")
        assert(req.params.getInt("b") == 200)
        Ok()
      },
      "/A/B/C/:a/:b/*",
      None
    )

    assert { h3(Get("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h3(Post("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h3(Put("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h3(Delete("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }

    assert { h3(Get("/a/B/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h3(Post("/A/b/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h3(Put("/A/B/c/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h3(Delete("/a/b/c/One/200/3000/d")).isInstanceOf[HttpRequest] }
  }

  it should "not have access to non-convertible parameter" in {
    val h1 = TargetedRequestHandler({ req => req.params.getInt("id"); Ok() }, "/:id", None)
    val h2 = TargetedRequestHandler({ req => req.params.getLong("id"); Ok() }, "/:id", None)

    assertThrows[ParameterNotConvertible](h1(Get("/a")))
    assertThrows[ParameterNotConvertible](h2(Get("/a")))
  }

  it should "not have access to missing parameter" in {
    val h1 = TargetedRequestHandler({ req => req.params.getString("id"); Ok() }, "/:identifier", None)
    val h2 = TargetedRequestHandler({ req => req.params.getInt("id"); Ok() }, "/:identifier", None)
    val h3 = TargetedRequestHandler({ req => req.params.getLong("id"); Ok() }, "/:identifier", None)

    assertThrows[ParameterNotFound](h1(Get("/a")))
    assertThrows[ParameterNotFound](h2(Get("/a")))
    assertThrows[ParameterNotFound](h3(Get("/a")))
  }

  it should "have invalid path" in {
    assertThrows[IllegalArgumentException](TargetedRequestHandler(req => Ok(), "a/b/c", None))
    assertThrows[IllegalArgumentException](TargetedRequestHandler(req => Ok(), "/a/*b/c", None))
  }
}
