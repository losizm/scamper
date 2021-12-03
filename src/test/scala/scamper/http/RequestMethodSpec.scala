
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

import java.io.{ File, FileInputStream }

import RequestMethod.Registry.*

class RequestMethodSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "get registered request method" in {
    assert(RequestMethod("GET") == Get)
    assert(RequestMethod("POST") == Post)
    assert(RequestMethod("PUT") == Put)
    assert(RequestMethod("PATCH") == Patch)
    assert(RequestMethod("DELETE") == Delete)
    assert(RequestMethod("HEAD") == Head)
    assert(RequestMethod("OPTIONS") == Options)
    assert(RequestMethod("TRACE") == Trace)
    assert(RequestMethod("CONNECT") == Connect)
  }

  it should "create request with message body" in {
    val req1 = Get("/a")
    assert(req1.isGet)
    assert(req1.target == Uri("/a"))

    val req2 = Post("/b?c=1&d=2&d=3")
    assert(req2.isPost)
    assert(req2.target == Uri("/b?c=1&d=2&d=3"))
  }
