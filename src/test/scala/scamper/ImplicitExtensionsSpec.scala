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

import java.net.{ URI, URL }
import java.time.{ LocalDate, LocalDateTime, OffsetDateTime }

import org.scalatest.FlatSpec

import ImplicitExtensions._

class ImplicitExtensionsSpec extends FlatSpec {
  val uri = new URI("/index.html")
  val url = new URL("http://localhost:8080/index.html")

  "URI" should "be created with new path" in {
    assert(uri.withPath("/home.html") == new URI("/home.html"))
  }

  it should "be created with new query" in {
    val newURI = new URI("/index.html?name=guest")
    assert(uri.withQuery("name=guest") == newURI)
    assert(uri.withQueryParams("name" -> "guest") == newURI)
  }

  it should "be converted to URL" in {
    val newURL = uri.toURL("http", "localhost:8080")
    assert(url == newURL)
  }

  "URL" should "be created with new path" in {
    assert(url.withPath("home.html") == new URL("http://localhost:8080/home.html"))
  }

  it should "be created with new query" in {
    val newURL = new URL("http://localhost:8080/index.html?name=guest")
    assert(url.withQuery("name=guest") == newURL)
    assert(url.withQueryParams("name" -> "guest") == newURL)
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
