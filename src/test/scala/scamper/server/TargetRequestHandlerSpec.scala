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
package scamper.server

import org.scalatest.FlatSpec

import scamper.Implicits.stringToUri
import scamper.{ HttpRequest, HttpResponse }
import scamper.RequestMethods.{ DELETE, GET, POST, PUT }
import scamper.ResponseStatuses.Ok

import Implicits.ServerHttpRequestType

class TargetedRequestHandlerSpec extends FlatSpec {
  "TargetedRequestHandler" should "respond to request" in {
    val handler = TargetedRequestHandler(req => Ok(), "/", None)
    assert { handler(GET("/")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(POST("/")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(PUT("/")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(DELETE("/")).asInstanceOf[HttpResponse].status == Ok }
  }

  it should "respond to request with certain request method" in {
    val handler = TargetedRequestHandler(req => Ok(), "/", Some(PUT))
    assert { handler(GET("/")).isInstanceOf[HttpRequest] }
    assert { handler(POST("/")).isInstanceOf[HttpRequest] }
    assert { handler(PUT("/")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(DELETE("/")).isInstanceOf[HttpRequest] }
  }

  it should "respond to request with certain path" in {
    val handler = TargetedRequestHandler(req => Ok(), "/a/b/c", None)
    assert { handler(GET("http://localhost:8080//a//b/../../a/b////c")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(POST("/a/.//b/c")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(PUT("/a/b/c")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(DELETE("/a/b/c")).asInstanceOf[HttpResponse].status == Ok }
  }

  it should "respond to request with certain path and request method" in {
    val handler = TargetedRequestHandler(req => Ok(), "/a/b/c", Some(POST))
    assert { handler(GET("/a/b/c")).isInstanceOf[HttpRequest] }
    assert { handler(POST("/a/b/c")).asInstanceOf[HttpResponse].status == Ok }
    assert { handler(PUT("/a/b/c")).isInstanceOf[HttpRequest] }
    assert { handler(DELETE("/a/b/c")).isInstanceOf[HttpRequest] }
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

    assert { h1(GET("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h1(POST("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h1(PUT("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h1(DELETE("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }

    assert { h1(GET("/a/B/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h1(POST("/A/b/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h1(PUT("/A/B/c/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h1(DELETE("/a/b/c/One/200/3000/d")).isInstanceOf[HttpRequest] }

    val h2 = TargetedRequestHandler(
      { req =>
        assert(req.params.getString("abc") == "One/200/3000/d")
        Ok()
      },
      "/A/B/C/*abc",
      None
    )

    assert { h2(GET("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h2(POST("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h2(PUT("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }
    assert { h2(DELETE("/A/B/C/One/200/3000/d")).isInstanceOf[HttpResponse] }

    assert { h2(GET("/a/B/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h2(POST("/A/b/C/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h2(PUT("/A/B/c/One/200/3000/d")).isInstanceOf[HttpRequest] }
    assert { h2(DELETE("/a/b/c/One/200/3000/d")).isInstanceOf[HttpRequest] }
  }

  it should "not have access to non-convertible parameter" in {
    val h1 = TargetedRequestHandler({ req => req.params.getInt("id"); Ok() }, "/:id", None)
    val h2 = TargetedRequestHandler({ req => req.params.getLong("id"); Ok() }, "/:id", None)

    assertThrows[ParameterNotConvertible](h1(GET("/a")))
    assertThrows[ParameterNotConvertible](h2(GET("/a")))
  }

  it should "not have access to missing parameter" in {
    val h1 = TargetedRequestHandler({ req => req.params.getString("id"); Ok() }, "/:identifier", None)
    val h2 = TargetedRequestHandler({ req => req.params.getInt("id"); Ok() }, "/:identifier", None)
    val h3 = TargetedRequestHandler({ req => req.params.getLong("id"); Ok() }, "/:identifier", None)

    assertThrows[ParameterNotFound](h1(GET("/a")))
    assertThrows[ParameterNotFound](h2(GET("/a")))
    assertThrows[ParameterNotFound](h3(GET("/a")))
  }

  it should "have invalid path" in {
    assertThrows[IllegalArgumentException](TargetedRequestHandler(req => Ok(), "a/b/c", None))
    assertThrows[IllegalArgumentException](TargetedRequestHandler(req => Ok(), "/a/*b/c", None))
  }
}
