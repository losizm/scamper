/*
 * Copyright 2025 Carlos Conyers
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
package server

import scala.language.implicitConversions

import RequestMethod.Registry.Get
import ResponseStatus.Registry.Ok

class ConditionalRequestHandlerSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "test conditional request handlers" in {
    val fooRequest = Get("/").putHeaders("foo" -> "yes")
    val barRequest = Get("/").putHeaders("bar" -> "yes")
    val bazRequest = Get("/").putHeaders("baz" -> "yes")
    val fooAndBarRequest = Get("/").putHeaders("foo" -> "yes", "bar" -> "yes")

    val fooResponse = Ok("foo")
    val barResponse = Ok("bar")
    val fooAndBarResponse = Ok("foo and bar")
    val fooOrBarResponse = Ok("foo or bar")
    val notFooResponse = Ok("not foo")
    val notFooAndBarResponse = Ok("not foo and bar")
    val notFooOrBarResponse = Ok("not foo or bar")

    val hasFoo: RequestPredicate = _.hasHeader("foo")
    val hasBar: RequestPredicate = _.hasHeader("bar")

    val handler1 = ConditionalRequestHandler(hasFoo, _ => fooResponse)
    assert { handler1(fooRequest) == fooResponse }
    assert { handler1(barRequest) == barRequest }
    assert { handler1(bazRequest) == bazRequest }
    assert { handler1(fooAndBarRequest) == fooResponse }

    val handler2 = ConditionalRequestHandler(hasBar, _ => barResponse)
    assert { handler2(fooRequest) == fooRequest }
    assert { handler2(barRequest) == barResponse }
    assert { handler2(bazRequest) == bazRequest }
    assert { handler2(fooAndBarRequest) == barResponse }

    val handler3 = ConditionalRequestHandler(hasFoo and hasBar, _ => fooAndBarResponse)
    assert { handler3(fooRequest) == fooRequest }
    assert { handler3(barRequest) == barRequest }
    assert { handler3(bazRequest) == bazRequest }
    assert { handler3(fooAndBarRequest) == fooAndBarResponse }

    val handler4 = ConditionalRequestHandler(hasFoo or hasBar, _ => fooOrBarResponse)
    assert { handler4(fooRequest) == fooOrBarResponse }
    assert { handler4(barRequest) == fooOrBarResponse }
    assert { handler4(bazRequest) == bazRequest }
    assert { handler4(fooAndBarRequest) == fooOrBarResponse }

    val handler5 = ConditionalRequestHandler(hasFoo.negate(), _ => notFooResponse)
    assert { handler5(fooRequest) == fooRequest }
    assert { handler5(barRequest) == notFooResponse }
    assert { handler5(bazRequest) == notFooResponse }
    assert { handler5(fooAndBarRequest) == fooAndBarRequest }

    val handler6 = ConditionalRequestHandler((hasFoo and hasBar).negate(), _ => notFooAndBarResponse)
    assert { handler6(fooRequest) == notFooAndBarResponse }
    assert { handler6(barRequest) == notFooAndBarResponse }
    assert { handler6(bazRequest) == notFooAndBarResponse }
    assert { handler6(fooAndBarRequest) == fooAndBarRequest }

    val handler7 = ConditionalRequestHandler((hasFoo or hasBar).negate(), _ => notFooOrBarResponse)
    assert { handler7(fooRequest) == fooRequest }
    assert { handler7(barRequest) == barRequest }
    assert { handler7(bazRequest) == notFooOrBarResponse }
    assert { handler7(fooAndBarRequest) == fooAndBarRequest }
  }
