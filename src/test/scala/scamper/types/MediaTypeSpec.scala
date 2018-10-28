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
package scamper.types

import org.scalatest.FlatSpec

class MediaTypeSpec extends FlatSpec {
  "MediaType" should "be created without parameters" in {
    val contentType = MediaType.parse("text/html")
    assert(contentType.mainType == "text")
    assert(contentType.subtype == "html")
    assert(contentType.isText)
    assert(contentType.toString == "text/html")
  }

  it should "be created with parameters" in {
    var contentType = MediaType.parse("text/html; charset=iso-8859-1")
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
  }

  it should "be destructured" in {
    val contentType = MediaType.parse("text/html; charset=iso-8859-1")

    contentType match {
      case MediaType(mainType, subtype, params) =>
        assert(mainType == contentType.mainType)
        assert(subtype == contentType.subtype)
        assert(params == contentType.params)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](MediaType.parse("(text)/html"))
    assertThrows[IllegalArgumentException](MediaType.parse("text/(html)"))
    assertThrows[IllegalArgumentException](MediaType.parse("text/html; charset"))
    assertThrows[IllegalArgumentException](MediaType.parse("text/html; charset=iso 8859 1"))
  }
}
