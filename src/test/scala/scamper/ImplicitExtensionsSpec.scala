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

import java.net.URI
import java.time.{ LocalDate, LocalDateTime, OffsetDateTime }

import org.scalatest.FlatSpec

import ImplicitExtensions._

class ImplicitExtensionsSpec extends FlatSpec {
  val uri = new URI("http://localhost:8080/index.html")
  val uriPath = new URI("/index.html")
  val uriPathWithParams = new URI("/index.html?name=guest")

  "URI" should "be created with new path" in {
    assert(uriPath.withPath("/home.html") == new URI("/home.html"))
  }

  it should "be created with new query" in {
    assert(uriPath.withQuery("name=guest") == uriPathWithParams)
    assert(uriPath.withQueryParams("name" -> "guest") == uriPathWithParams)
  }

  it should "created with new scheme and authority" in {
    assert(uriPath.withScheme("http").withAuthority("localhost:8080") == uri)
  }

  "String" should "be converted to LocalDate" in {
    assert("2006-02-14".toLocalDate == LocalDate.parse("2006-02-14"))
  }

  it should "be converted to LocalDateTime" in {
    assert("2006-02-14T11:15:37".toLocalDateTime == LocalDateTime.parse("2006-02-14T11:15:37"))
  }

  it should "be converted to OffsetDateTime" in {
    assert("2006-02-14T11:15:37-05:00".toOffsetDateTime == OffsetDateTime.parse("2006-02-14T11:15:37-05:00"))
    assert("Tue, 14 Feb 2006 11:15:37 -0500".toOffsetDateTime == OffsetDateTime.parse("2006-02-14T11:15:37-05:00"))
  }
}
