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
package scamper

import scamper.RequestMethod.Registry._

class RequestLineSpec extends org.scalatest.flatspec.AnyFlatSpec {
  "RequestLine" should "be created" in {
    var request = RequestLine("GET / HTTP/1.1")
    assert(request.method == GET)
    assert(request.target.toString == "/")
    assert(request.version == HttpVersion(1, 1))

    request = RequestLine("GET /index.html HTTP/1.1")
    assert(request.method == GET)
    assert(request.target.toString == "/index.html")
    assert(request.version == HttpVersion(1, 1))

    request = RequestLine("GET /index.html?offset=25&limit=5 HTTP/1.1")
    assert(request.method == GET)
    assert(request.target.toString == "/index.html?offset=25&limit=5")
    assert(request.version == HttpVersion(1, 1))

    request = RequestLine("POST https://localhost:8787/admin/user/create HTTP/1.1")
    assert(request.method == POST)
    assert(request.target.toString == "https://localhost:8787/admin/user/create")
    assert(request.version == HttpVersion(1, 1))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](RequestLine("GET"))
    assertThrows[IllegalArgumentException](RequestLine("GET /index.html"))
    assertThrows[IllegalArgumentException](RequestLine("GET HTTP/1.1"))
  }
}
