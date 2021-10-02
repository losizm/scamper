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
package scamper.types

import java.io.File

class MediaTypeSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create MediaType without parameters" in {
    val contentType = MediaType("text/html")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.isText)
    assert(contentType.toString == "text/html")
  }

  it should "create MediaType with parameters" in {
    var contentType = MediaType("text/html; charset=iso-8859-1")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "iso-8859-1")
    assert(contentType.isText)
    assert(contentType.toString == "text/html; charset=iso-8859-1")

    contentType = MediaType("text", "html", "charset" -> "iso-8859-1")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "iso-8859-1")
    assert(contentType.isText)
    assert(contentType.toString == "text/html; charset=iso-8859-1")

    contentType = MediaType("text", "html", "charset" -> "utf-8", "not-a-charset" -> "iso 8859 1")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "utf-8")
    assert(contentType.params("not-a-charset") == "iso 8859 1")
    assert(contentType.isText)
    assert(contentType.toString == "text/html; charset=utf-8; not-a-charset=\"iso 8859 1\"")

    contentType = MediaType("text/html").setParams("charset" -> "utf-8", "not-a-charset" -> "iso 8859 1")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.params("charset") == "utf-8")
    assert(contentType.params("not-a-charset") == "iso 8859 1")
    assert(contentType.isText)
    assert(contentType.toString == "text/html; charset=utf-8; not-a-charset=\"iso 8859 1\"")
  }

  it should "get text/plain" in {
    assert(MediaType.plain == MediaType("text/plain"))
  }

  it should "get application/octet-stream" in {
    assert(MediaType.octetStream == MediaType("application/octet-stream"))
  }

  it should "get MediaType based on file" in {
    assert(MediaType.forFile(File("test.xml"))  contains MediaType("application/xml"))
    assert(MediaType.forFile(File("test.json")) contains MediaType("application/json"))
    assert(MediaType.forFile(File("test.html")) contains MediaType("text/html"))
    assert(MediaType.forFile(File("test.css"))  contains MediaType("text/css"))
    assert(MediaType.forFile(File("test.js"))   contains MediaType("text/javascript"))
    assert(MediaType.forFile(File("test.jpeg")) contains MediaType("image/jpeg"))
    assert(MediaType.forFile(File("test.gif"))  contains MediaType("image/gif"))
    assert(MediaType.forFile(File("test.exe"))  contains MediaType("application/octet-stream"))
    assert(MediaType.forFile(File("test.xyz")).isEmpty)
  }

  it should "get MediaType based on file name" in {
    assert(MediaType.forFileName("test.xml")  contains MediaType("application/xml"))
    assert(MediaType.forFileName("test.json") contains MediaType("application/json"))
    assert(MediaType.forFileName("test.html") contains MediaType("text/html"))
    assert(MediaType.forFileName("test.css")  contains MediaType("text/css"))
    assert(MediaType.forFileName("test.js")   contains MediaType("text/javascript"))
    assert(MediaType.forFileName("test.jpeg") contains MediaType("image/jpeg"))
    assert(MediaType.forFileName("test.gif")  contains MediaType("image/gif"))
    assert(MediaType.forFileName("test.exe")  contains MediaType("application/octet-stream"))
    assert(MediaType.forFileName("test.xyz").isEmpty)
  }

  it should "get MediaType based on file suffix" in {
    assert(MediaType.forSuffix(".xml")  contains MediaType("application/xml"))
    assert(MediaType.forSuffix(".json") contains MediaType("application/json"))
    assert(MediaType.forSuffix(".html") contains MediaType("text/html"))
    assert(MediaType.forSuffix(".css")  contains MediaType("text/css"))
    assert(MediaType.forSuffix(".js")   contains MediaType("text/javascript"))
    assert(MediaType.forSuffix(".jpeg") contains MediaType("image/jpeg"))
    assert(MediaType.forSuffix(".gif")  contains MediaType("image/gif"))
    assert(MediaType.forSuffix(".exe")  contains MediaType("application/octet-stream"))
    assert(MediaType.forSuffix(".xyz").isEmpty)

    assert(MediaType.forSuffix("xml")  contains MediaType("application/xml"))
    assert(MediaType.forSuffix("json") contains MediaType("application/json"))
    assert(MediaType.forSuffix("html") contains MediaType("text/html"))
    assert(MediaType.forSuffix("css")  contains MediaType("text/css"))
    assert(MediaType.forSuffix("js")   contains MediaType("text/javascript"))
    assert(MediaType.forSuffix("jpeg") contains MediaType("image/jpeg"))
    assert(MediaType.forSuffix("gif")  contains MediaType("image/gif"))
    assert(MediaType.forSuffix("exe")  contains MediaType("application/octet-stream"))
    assert(MediaType.forSuffix("xyz").isEmpty)
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](MediaType("(text)/html"))
    assertThrows[IllegalArgumentException](MediaType("text/(html)"))
    assertThrows[IllegalArgumentException](MediaType("text/html; charset"))
    assertThrows[IllegalArgumentException](MediaType("text/html; charset=iso 8859 1"))
  }
