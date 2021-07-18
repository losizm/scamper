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

class StatusLineSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "StatusLine" should "be created" in {
    var response = StatusLine("HTTP/1.1 200 OK")
    assert(response.version == HttpVersion(1, 1))
    assert(response.status == ResponseStatus(200, "OK"))
    assert(response.status eq ResponseStatus(200, "OK"))
    assert(response.status ne ResponseStatus(200, "ok"))

    response = StatusLine("HTTP/1.1 400 Bad Request")
    assert(response.version == HttpVersion(1, 1))
    assert(response.status == ResponseStatus(400, "Bad Request"))
    assert(response.status eq ResponseStatus(400, "Bad Request"))
    assert(response.status ne ResponseStatus(400, "bad request"))

    response = StatusLine("HTTP/1.1 500 Internal Server Error")
    assert(response.version == HttpVersion(1, 1))
    assert(response.status == ResponseStatus(500, "Internal Server Error"))
    assert(response.status eq ResponseStatus(500, "Internal Server Error"))
    assert(response.status ne ResponseStatus(500, "internal server error"))

    response = StatusLine("HTTP/2 200")
    assert(response.version == HttpVersion(2, 0))
    assert(response.status == ResponseStatus(200, "OK"))
    assert(response.status eq ResponseStatus(200, "OK"))
    assert(response.status ne ResponseStatus(200, "ok"))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](StatusLine("HTTP/1.1"))
    assertThrows[IllegalArgumentException](StatusLine("HTTP/1.1-200"))
    assertThrows[IllegalArgumentException](StatusLine("HTTP/ OK"))
  }
