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

import scamper.ResponseStatuses.Ok
import scamper.headers.ContentLength

class ResponseFilterSpec extends FlatSpec {
  "ResponseFilter" should "be composed with another" in {
    val f1: ResponseFilter = _.withContentLength(10)
    val f2: ResponseFilter = _.withContentLength(20)
    val res = Ok()

    assert { f1.compose(f2).apply(res).contentLength == 10 }
    assert { f2.compose(f1).apply(res).contentLength == 20 }
    assert { f1.andThen(f2).apply(res).contentLength == 20 }
    assert { f2.andThen(f1).apply(res).contentLength == 10 }
  }
}