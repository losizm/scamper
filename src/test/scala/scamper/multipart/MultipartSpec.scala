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
package multipart

import java.io.File

import scamper.types.{ DispositionType, MediaType }

class MultipartSpec extends org.scalatest.flatspec.AnyFlatSpec:
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
    val formData = DispositionType("form-data", "name" -> "id")
    val formDataNoName = DispositionType("form-data")
    val attachment = DispositionType("attachment", "filename" -> "photo.jpg")
    val octetStream = MediaType("application", "octet-stream")
    val header = Header("Content-Type", "text/plain")

    assertThrows[HttpException] { TextPart(formDataNoName, "Hello, world") }
    assertThrows[HttpException] { TextPart(formData, octetStream, "Hello, world") }
    assertThrows[HttpException] { TextPart(attachment, "Hello, world") }
    assertThrows[HeaderNotFound] { TextPart(Seq(header), "Hello, world") }
  }

  "FilePart" should "be created" in {
    var part = FilePart("photo", File("photo.jpg"))
    assert(part.name == "photo")
    assert(part.content == File("photo.jpg"))
    assert(part.getFileName.contains("photo.jpg"))
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "photo")
    assert(part.contentDisposition.params("filename") == "photo.jpg")
    assert(part.contentType.isImage)
    assert(part.contentType.subtype == "jpeg")

    part = FilePart("photo", File("photo.jpg"), "my-photo.jpg")
    assert(part.name == "photo")
    assert(part.content == File("photo.jpg"))
    assert(part.getFileName.contains("my-photo.jpg"))
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "photo")
    assert(part.contentDisposition.params("filename") == "my-photo.jpg")
    assert(part.contentType.isImage)
    assert(part.contentType.subtype == "jpeg")

    part = FilePart("photo", File("photo.jpg"), Some("my-photo.jpg"))
    assert(part.name == "photo")
    assert(part.content == File("photo.jpg"))
    assert(part.getFileName.contains("my-photo.jpg"))
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "photo")
    assert(part.contentDisposition.params("filename") == "my-photo.jpg")
    assert(part.contentType.isImage)
    assert(part.contentType.subtype == "jpeg")

    part = FilePart("photo", File("photo.jpg"), None)
    assert(part.name == "photo")
    assert(part.content == File("photo.jpg"))
    assert(part.getFileName.isEmpty)
    assert(part.contentDisposition.name == "form-data")
    assert(part.contentDisposition.params("name") == "photo")
    assert(part.contentDisposition.params.get("filename").isEmpty)
    assert(part.contentType.isImage)
    assert(part.contentType.subtype == "jpeg")
  }

  it should "not be created in" in {
    val formData = DispositionType("form-data", "name" -> "id")
    val formDataNoName = DispositionType("form-data")
    val attachment = DispositionType("attachment", "filename" -> "photo.jpg")
    val header = Header("Content-Type", "text/plain")
    val content = File("photo.jpg")

    assertThrows[HttpException] { FilePart(formDataNoName, content) }
    assertThrows[HttpException] { FilePart(attachment, content) }
    assertThrows[HeaderNotFound] { FilePart(Seq(header), content) }
  }

  "Multipart" should "be created" in {
    val id = TextPart("id", "root")
    val photo = FilePart("photo", File("photo.jpg"))
    val rap = TextPart("genre", "Rap")
    val rnb = TextPart("genre", "R&B")
    val reggae = TextPart("genre", "Reggae")
    val multipart = Multipart(id, photo, rap, rnb, reggae)

    assert(multipart.parts.sameElements(Seq(id, photo, rap, rnb, reggae)))

    assert(multipart.getPart("id").contains(id))
    assert(multipart.getParts("id").sameElements(Seq(id)))
    assert(multipart.getTextPart("id").contains(id))
    assert(multipart.getText("id").contains("root"))
    assertThrows[ClassCastException] { multipart.getFilePart("id") }
    assertThrows[ClassCastException] { multipart.getFile("id") }

    assert(multipart.getPart("photo").contains(photo))
    assert(multipart.getParts("photo").sameElements(Seq(photo)))
    assert(multipart.getFilePart("photo").contains(photo))
    assert(multipart.getFile("photo").contains(File("photo.jpg")))
    assertThrows[ClassCastException] { multipart.getTextPart("photo") }
    assertThrows[ClassCastException] { multipart.getText("photo") }

    assert(multipart.getPart("genre").contains(rap))
    assert(multipart.getParts("genre").sameElements(Seq(rap, rnb, reggae)))
    assert(multipart.getTextPart("genre").contains(rap))
    assert(multipart.getText("genre").contains("Rap"))
    assertThrows[ClassCastException] { multipart.getFilePart("genre") }
    assertThrows[ClassCastException] { multipart.getFile("genre") }

    assert(multipart.getPart("none").isEmpty)
    assert(multipart.getParts("none").isEmpty)
    assert(multipart.getTextPart("none").isEmpty)
    assert(multipart.getFilePart("none").isEmpty)
    assert(multipart.getText("none").isEmpty)
    assert(multipart.getFile("none").isEmpty)

    assert(multipart.textParts.sameElements(Seq(id, rap, rnb, reggae)))
    assert(multipart.fileParts.sameElements(Seq(photo)))

    val query = multipart.toQuery
    assert(query("id") == "root")
    assert(query.getValues("id") == Seq("root"))
    assert(query("genre") == "Rap")
    assert(query.getValues("genre").sameElements(Seq("Rap", "R&B", "Reggae")))
    assertThrows[NoSuchElementException] { query("photo") }
  }
