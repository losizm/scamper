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
package scamper.server

import java.io.File

import scamper.Auxiliary.StringType
import scamper.ResponseStatus.Registry.Ok
import scamper.headers.{ ContentDisposition, ContentLength, ContentType }
import scamper.types.MediaType

class ServerHttpResponseSpec extends org.scalatest.flatspec.AnyFlatSpec:
  val file = File("./src/test/resources/test.html")
  val encodedFileName = s"utf-8''${file.getName.toUrlEncoded("utf-8")}"

  it should "add file attachment to HttpResponse" in {
    val res = Ok().setAttachment(file)

    assert { res.contentType == MediaType("text", "html") }
    assert { res.contentLength == file.length }
    assert { res.contentDisposition.isAttachment }
    assert { res.contentDisposition.name == "attachment" }
    assert { res.contentDisposition.params("filename") == file.getName }
    assert { res.contentDisposition.params("filename*") == encodedFileName }
  }

  it should "add inline content to HttpResponse" in {
    val res = Ok().setInline(file)

    assert { res.contentType == MediaType("text", "html") }
    assert { res.contentLength == file.length }
    assert { res.contentDisposition.isInline }
    assert { res.contentDisposition.name == "inline" }
    assert { res.contentDisposition.params("filename") == file.getName }
    assert { res.contentDisposition.params("filename*") == encodedFileName }
  }
