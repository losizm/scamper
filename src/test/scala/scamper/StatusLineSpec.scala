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
package scamper

import org.scalatest.FlatSpec

class StatusLineSpec extends FlatSpec {
  "StatusLine" should "be created" in {
    var response = StatusLine.parse("HTTP/1.1 200 OK")
    assert(response.version == HttpVersion(1, 1))
    assert(response.status == ResponseStatus(200, "OK"))

    response = StatusLine.parse("HTTP/1.1 400 Bad Request")
    assert(response.version == HttpVersion(1, 1))
    assert(response.status == ResponseStatus(400, "Bad Request"))

    response = StatusLine.parse("HTTP/1.1 500 Internal Server Error")
    assert(response.version == HttpVersion(1, 1))
    assert(response.status == ResponseStatus(500, "Internal Server Error"))

    response = StatusLine.parse("HTTP/2 200")
    assert(response.version == HttpVersion(2, 0))
    assert(response.status == ResponseStatus(200, "OK"))
  }

  it should "not be created" in {
    assertThrows[IllegalArgumentException](StatusLine.parse("HTTP/1.1"))
    assertThrows[IllegalArgumentException](StatusLine.parse("HTTP/1.1-200"))
    assertThrows[IllegalArgumentException](StatusLine.parse("HTTP/ OK"))
  }
}
