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

import java.io.File

import org.scalatest.FlatSpec

import scamper.types.{ DispositionType, MediaType }

class MultipartSpec extends FlatSpec {
  "TextPart" should "be created" in {
    val part = TextPart("id", "root")
    assert(part.name == "id")
    assert(part.content == "root")
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "id")
    assert(part.contentType.isText)
    assert(part.contentType.subtype == "plain")
  }

  it should "not be created in" in {
    val formData = DispositionType("form-data", Map("name" -> "id"))
    val formDataNoName = DispositionType("form-data")
    val attachment = DispositionType("attachment", Map("filename" -> "photo.jpg"))
    val octetStream = MediaType("application", "octet-stream")
    val header = Header("Content-Type", "text/plain")

    assertThrows[HttpException] { TextPart(formDataNoName, "Hello, world") }
    assertThrows[HttpException] { TextPart(formData, octetStream, "Hello, world") }
    assertThrows[HttpException] { TextPart(attachment, "Hello, world") }
    assertThrows[HeaderNotFound] { TextPart(Seq(header), "Hello, world") }
  }

  "FilePart" should "be created" in {
    var part = FilePart("photo", new File("photo.jpg"))
    assert(part.name == "photo")
    assert(part.content == new File("photo.jpg"))
    assert(part.getFileName.contains("photo.jpg"))
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "photo")
    assert(part.contentDisposition.params("filename") == "photo.jpg")
    assert(part.contentType.isApplication)
    assert(part.contentType.subtype == "octet-stream")

    part = FilePart("photo", new File("photo.jpg"), "my-photo.jpg")
    assert(part.name == "photo")
    assert(part.content == new File("photo.jpg"))
    assert(part.getFileName.contains("my-photo.jpg"))
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "photo")
    assert(part.contentDisposition.params("filename") == "my-photo.jpg")
    assert(part.contentType.isApplication)
    assert(part.contentType.subtype == "octet-stream")

    part = FilePart("photo", new File("photo.jpg"), Some("my-photo.jpg"))
    assert(part.name == "photo")
    assert(part.content == new File("photo.jpg"))
    assert(part.getFileName.contains("my-photo.jpg"))
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "photo")
    assert(part.contentDisposition.params("filename") == "my-photo.jpg")
    assert(part.contentType.isApplication)
    assert(part.contentType.subtype == "octet-stream")

    part = FilePart("photo", new File("photo.jpg"), None)
    assert(part.name == "photo")
    assert(part.content == new File("photo.jpg"))
    assert(part.getFileName.isEmpty)
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "photo")
    assert(part.contentDisposition.params.get("filename").isEmpty)
    assert(part.contentType.isApplication)
    assert(part.contentType.subtype == "octet-stream")
  }

  it should "not be created in" in {
    val formData = DispositionType("form-data", Map("name" -> "id"))
    val formDataNoName = DispositionType("form-data")
    val attachment = DispositionType("attachment", Map("filename" -> "photo.jpg"))
    val octetStream = MediaType("application", "octet-stream")
    val header = Header("Content-Type", "text/plain")
    val content = new File("photo.jpg")

    assertThrows[HttpException] { FilePart(formDataNoName, content) }
    assertThrows[HttpException] { FilePart(attachment, content) }
    assertThrows[HeaderNotFound] { FilePart(Seq(header), content) }
  }

  "Multipart" should "be created" in {
    val id = TextPart("id", "root")
    val photo = FilePart("photo", new File("photo.jpg"))
    val rap = TextPart("genre", "Rap")
    val rnb = TextPart("genre", "R&B")
    val reggae = TextPart("genre", "Reggae")
    val multipart = Multipart(id, photo, rap, rnb, reggae)

    assert(multipart.parts.sameElements(Seq(id, photo, rap, rnb, reggae)))

    assert(multipart.getPart("id").contains(id))
    assert(multipart.getParts("id").sameElements(Seq(id)))
    assert(multipart.getTextPart("id").contains(id))
    assertThrows[ClassCastException] { multipart.getFilePart("id") }

    assert(multipart.getPart("photo").contains(photo))
    assert(multipart.getParts("photo").sameElements(Seq(photo)))
    assert(multipart.getFilePart("photo").contains(photo))
    assertThrows[ClassCastException] { multipart.getTextPart("photo") }

    assert(multipart.getPart("genre").contains(rap))
    assert(multipart.getParts("genre").sameElements(Seq(rap, rnb, reggae)))
    assert(multipart.getTextPart("genre").contains(rap))
    assertThrows[ClassCastException] { multipart.getFilePart("genre") }

    assert(multipart.getPart("none").isEmpty)
    assert(multipart.getParts("none").isEmpty)
    assert(multipart.getTextPart("none").isEmpty)
    assert(multipart.getFilePart("none").isEmpty)
  }
}

