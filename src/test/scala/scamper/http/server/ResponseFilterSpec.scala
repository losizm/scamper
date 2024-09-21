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
package server

import scala.language.implicitConversions

import scamper.http.headers.toContentLength

import ResponseStatus.Registry.Ok

class ResponseFilterSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "compose response filters" in {
    val f1: ResponseFilter = _.setContentLength(10)
    val f2: ResponseFilter = _.setContentLength(20)
    val res = Ok()

    assert { f1.after(f2).apply(res).contentLength == 10 }
    assert { f2.after(f1).apply(res).contentLength == 20 }
    assert { f1.before(f2).apply(res).contentLength == 20 }
    assert { f2.before(f1).apply(res).contentLength == 10 }
  }
