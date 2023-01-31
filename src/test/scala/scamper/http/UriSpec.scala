/*
 * Copyright 2023 Carlos Conyers
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

class UriSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create URI from String" in {
    val uri = ShowUri("/index.html")
    assert { !uri.isAbsolute }
    assert { uri.schemeOption.isEmpty }
    assert { uri.authorityOption.isEmpty }
    assert { uri.hostOption.isEmpty }
    assert { uri.portOption.isEmpty }
    assert { uri.path == "/index.html" }
    assert { uri.query.isEmpty }
    assert { uri.fragmentOption.isEmpty }
    assert { uri.toString == "/index.html" }

    assertThrows[NoSuchElementException] { uri.scheme }
    assertThrows[NoSuchElementException] { uri.authority }
    assertThrows[NoSuchElementException] { uri.host }
    assertThrows[NoSuchElementException] { uri.port }
    assertThrows[NoSuchElementException] { uri.fragment }
  }

  it should "create URI from scheme, scheme-specific part, and fragment" in {
    val uri = ShowUri("/index.html?a=1&b=2#top")
    assert { !uri.isAbsolute }
    assert { uri.schemeOption.isEmpty }
    assert { uri.authorityOption.isEmpty }
    assert { uri.hostOption.isEmpty }
    assert { uri.portOption.isEmpty }
    assert { uri.path == "/index.html" }
    assert { uri.query == QueryString("a=1&b=2") }
    assert { uri.fragment == "top" }
    assert { uri.toString == "/index.html?a=1&b=2#top" }

    assertThrows[NoSuchElementException] { uri.scheme }
    assertThrows[NoSuchElementException] { uri.authority }
    assertThrows[NoSuchElementException] { uri.host }
    assertThrows[NoSuchElementException] { uri.port }
  }

  it should "create absolute URI from String" in {
    val uri = ShowUri("http://localhost:8080/index.html")
    assert { uri.isAbsolute }
    assert { uri.scheme == "http" }
    assert { uri.authority == "localhost:8080" }
    assert { uri.host == "localhost" }
    assert { uri.port == 8080 }
    assert { uri.path == "/index.html" }
    assert { uri.query.isEmpty }
    assert { uri.fragmentOption.isEmpty }
    assert { uri.toString == "http://localhost:8080/index.html" }

    assertThrows[NoSuchElementException] { uri.fragment }
  }

  it should "create absolute URI from scheme, scheme-specific part, and fragment" in {
    val uri = ShowUri("http", "//localhost:8080/index.html?a=1&b=2", Some("top"))
    assert { uri.isAbsolute }
    assert { uri.scheme == "http" }
    assert { uri.authority == "localhost:8080" }
    assert { uri.host == "localhost" }
    assert { uri.port == 8080 }
    assert { uri.path == "/index.html" }
    assert { uri.query == QueryString("a=1&b=2") }
    assert { uri.fragment == "top" }
    assert { uri.toString == "http://localhost:8080/index.html?a=1&b=2#top" }
  }

  it should "create relative URI from absolute URI" in {
    val uri = ShowUri(Uri("http://localhost:8080/index.html?a=1&b=2#top").toRelativeUri)

    assert { !uri.isAbsolute }
    assert { uri.schemeOption.isEmpty }
    assert { uri.authorityOption.isEmpty }
    assert { uri.hostOption.isEmpty }
    assert { uri.portOption.isEmpty }
    assert { uri.path == "/index.html" }
    assert { uri.query == QueryString("a=1&b=2") }
    assert { uri.fragment == "top" }
    assert { uri.toString == "/index.html?a=1&b=2#top" }

    assertThrows[NoSuchElementException] { uri.scheme }
    assertThrows[NoSuchElementException] { uri.authority }
    assertThrows[NoSuchElementException] { uri.host }
    assertThrows[NoSuchElementException] { uri.port }
  }

  it should "create absolute URI from relative URI" in {
    val uri = ShowUri(Uri("/index.html?a=1&b=2#top").toAbsoluteUri("http", "localhost"))
    assert { uri.isAbsolute }
    assert { uri.scheme == "http" }
    assert { uri.authority == "localhost" }
    assert { uri.host == "localhost" }
    assert { uri.portOption.isEmpty }
    assert { uri.path == "/index.html" }
    assert { uri.query == QueryString("a=1&b=2") }
    assert { uri.fragment == "top" }
    assert { uri.toString == "http://localhost/index.html?a=1&b=2#top" }

    assertThrows[NoSuchElementException] { uri.port }
  }

  it should "not create URI with scheme and no authority" in {
    assertThrows[IllegalArgumentException] { Uri("https:/index.html") }
    assertThrows[IllegalArgumentException] { Uri("https:/") }

    assertThrows[IllegalArgumentException] { Uri("http:/index.html") }
    assertThrows[IllegalArgumentException] { Uri("http:/") }

    assertThrows[IllegalArgumentException] { Uri("wss:/websocket") }
    assertThrows[IllegalArgumentException] { Uri("wss:/") }

    assertThrows[IllegalArgumentException] { Uri("ws:/websocket") }
    assertThrows[IllegalArgumentException] { Uri("ws:/") }
  }

  it should "not create URI with authority and no scheme" in {
    assertThrows[IllegalArgumentException] { Uri("//localhost:8080/index.html") }
    assertThrows[IllegalArgumentException] { Uri("//localhost:8080/websocket") }
  }

  private def ShowUri(scheme: String, schemePart: String, fragment: Option[String] = None): Uri =
    ShowUri(Uri(scheme, schemePart, fragment))

  private def ShowUri(uri: String): Uri =
    ShowUri(Uri(uri))

  private def ShowUri(uri: Uri): uri.type =
    info(s"uri:        $uri")
    info(s"isAbsolute: ${uri.isAbsolute}")
    info(s"scheme:     ${uri.schemeOption}")
    info(s"authority:  ${uri.authorityOption}")
    info(s"host:       ${uri.hostOption}")
    info(s"port:       ${uri.portOption}")
    info(s"path:       ${uri.path}")
    info(s"query:      ${uri.query}")
    info(s"fragment:   ${uri.fragmentOption}")
    uri
