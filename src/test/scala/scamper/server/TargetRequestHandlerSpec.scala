/*
 * Copyright 2018 Carlos Conyers
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

import scamper.ImplicitConverters.stringToUri
import scamper.RequestMethods.{ DELETE, GET, POST, PUT }
import scamper.ResponseStatuses.Ok

import Implicits.ServerHttpRequestType

class TargetedRequestHandlerSpec extends FlatSpec {
  "TargetedRequestHandler" should "respond to request" in {
    val handler = TargetedRequestHandler(req => Right(Ok()), "/", None)
    assert(handler(GET("/")).exists(_.status == Ok))
    assert(handler(POST("/")).exists(_.status == Ok))
    assert(handler(PUT("/")).exists(_.status == Ok))
    assert(handler(DELETE("/")).exists(_.status == Ok))
  }

  it should "respond only to request with certain request method" in {
    val handler = TargetedRequestHandler(req => Right(Ok()), "/", Some(PUT))
    assert(handler(GET("/")).isLeft)
    assert(handler(POST("/")).isLeft)
    assert(handler(PUT("/")).exists(_.status == Ok))
    assert(handler(DELETE("/")).isLeft)
  }

  it should "respond only to request with certain path" in {
    val handler = TargetedRequestHandler(req => Right(Ok()), "/a/b/c", None)
    assert(handler(GET("/a/b/c")).exists(_.status == Ok))
    assert(handler(POST("/a/b/c")).exists(_.status == Ok))
    assert(handler(PUT("/a/b/c")).exists(_.status == Ok))
    assert(handler(DELETE("/a/b/c")).exists(_.status == Ok))
  }

  it should "respond only to request with certain path and request method" in {
    val handler = TargetedRequestHandler(req => Right(Ok()), "/a/b/c", Some(POST))
    assert(handler(GET("/a/b/c")).isLeft)
    assert(handler(POST("/a/b/c")).exists(_.status == Ok))
    assert(handler(PUT("/a/b/c")).isLeft)
    assert(handler(DELETE("/a/b/c")).isLeft)
  }

  it should "have access to request parameters" in {
    val h1 = TargetedRequestHandler(
      { req =>
        assert(req.params.getString("a") == "One")
        assert(req.params.getInt("b") == 200)
        assert(req.params.getLong("c") == 3000)
        Right(Ok())
      },
      "/A/B/C/:a/:b/:c/d",
      None
    )

    assert(h1(GET("/A/B/C/One/200/3000/d")).isRight)
    assert(h1(POST("/A/B/C/One/200/3000/d")).isRight)
    assert(h1(PUT("/A/B/C/One/200/3000/d")).isRight)
    assert(h1(DELETE("/A/B/C/One/200/3000/d")).isRight)

    assert(h1(GET("/a/B/C/One/200/3000/d")).isLeft)
    assert(h1(POST("/A/b/C/One/200/3000/d")).isLeft)
    assert(h1(PUT("/A/B/c/One/200/3000/d")).isLeft)
    assert(h1(DELETE("/a/b/c/One/200/3000/d")).isLeft)

    val h2 = TargetedRequestHandler(
      { req =>
        assert(req.params.getString("abc") == "One/200/3000/d")
        Right(Ok())
      },
      "/A/B/C/*abc",
      None
    )

    assert(h2(GET("/A/B/C/One/200/3000/d")).isRight)
    assert(h2(POST("/A/B/C/One/200/3000/d")).isRight)
    assert(h2(PUT("/A/B/C/One/200/3000/d")).isRight)
    assert(h2(DELETE("/A/B/C/One/200/3000/d")).isRight)

    assert(h2(GET("/a/B/C/One/200/3000/d")).isLeft)
    assert(h2(POST("/A/b/C/One/200/3000/d")).isLeft)
    assert(h2(PUT("/A/B/c/One/200/3000/d")).isLeft)
    assert(h2(DELETE("/a/b/c/One/200/3000/d")).isLeft)
  }

  it should "not have access to non-convertible parameter" in {
    val h1 = TargetedRequestHandler({ req => req.params.getInt("id"); Right(Ok()) }, "/:id", None)
    val h2 = TargetedRequestHandler({ req => req.params.getLong("id"); Right(Ok()) }, "/:id", None)

    assertThrows[ParameterNotConvertible](h1(GET("/a")))
    assertThrows[ParameterNotConvertible](h2(GET("/a")))
  }

  it should "not have access to missing parameter" in {
    val h1 = TargetedRequestHandler({ req => req.params.getString("id"); Right(Ok()) }, "/:identifier", None)
    val h2 = TargetedRequestHandler({ req => req.params.getInt("id"); Right(Ok()) }, "/:identifier", None)
    val h3 = TargetedRequestHandler({ req => req.params.getLong("id"); Right(Ok()) }, "/:identifier", None)

    assertThrows[ParameterNotFound](h1(GET("/a")))
    assertThrows[ParameterNotFound](h2(GET("/a")))
    assertThrows[ParameterNotFound](h3(GET("/a")))
  }

  it should "have invalid path" in {
    assertThrows[IllegalArgumentException](TargetedRequestHandler(req => Right(Ok()), "a/b/c", None))
    assertThrows[IllegalArgumentException](TargetedRequestHandler(req => Right(Ok()), "/a/*b/c", None))
  }
}
