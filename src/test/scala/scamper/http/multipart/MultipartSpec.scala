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
package http
package multipart

import java.io.File

import scamper.http.types.{ DispositionType, MediaType }

class MultipartSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "create part with string content" in {
    val part1 = Part("id", "root")
    assert(part1.name == "id")
    assert(part1.fileName.isEmpty)
    assert(part1.getString() == "root")
    assert(part1.getBytes() sameElements "root".getBytes("UTF-8"))
    assert(part1.getFile().getBytes() sameElements "root".getBytes("UTF-8"))
    assert(part1.size == "root".getBytes("UTF-8").size)
    assert(part1.contentDisposition.name == "form-data")
    assert(part1.contentDisposition.params("name") == "id")
    assert(part1.contentType.isText)
    assert(part1.contentType.subtypeName == "plain")
    assert(!part1.contentType.params.contains("charset"))

    val part2 = Part(DispositionType("form-data", "name" -> "id"), MediaType.plain("UTF-8"), "root")
    assert(part2.name == "id")
    assert(part2.fileName.isEmpty)
    assert(part2.getString() == "root")
    assert(part2.getBytes() sameElements "root".getBytes("UTF-8"))
    assert(part2.getFile().getBytes() sameElements "root".getBytes("UTF-8"))
    assert(part2.size == "root".getBytes("UTF-8").size)
    assert(part2.contentDisposition.name == "form-data")
    assert(part2.contentDisposition.params("name") == "id")
    assert(part2.contentType.isText)
    assert(part2.contentType.subtypeName == "plain")
    assert(part2.contentType.params.get("charset").contains("UTF-8"))
  }

  it should "create part with byte array content" in {
    val secret = "letmein".getBytes("ASCII")

    val part1 = Part("secret", secret)
    assert(part1.name == "secret")
    assert(part1.fileName.isEmpty)
    assert(part1.getBytes() sameElements secret)
    assert(part1.getString() == String(secret, "ASCII"))
    assert(part1.getFile().getBytes() sameElements secret)
    assert(part1.size == secret.size)
    assert(part1.contentDisposition.name == "form-data")
    assert(part1.contentDisposition.params("name") == "secret")
    assert(part1.contentType.isApplication)
    assert(part1.contentType.subtypeName == "octet-stream")
    assert(!part1.contentType.params.contains("charset"))

    val part2 = Part(DispositionType("form-data", "name" -> "secret"), MediaType("application/password"), secret)
    assert(part2.name == "secret")
    assert(part2.fileName.isEmpty)
    assert(part2.getBytes() sameElements secret)
    assert(part2.getString() == String(secret, "ASCII"))
    assert(part2.getFile().getBytes() sameElements secret)
    assert(part2.size == secret.size)
    assert(part2.contentDisposition.name == "form-data")
    assert(part2.contentDisposition.params("name") == "secret")
    assert(part2.contentType.isApplication)
    assert(part2.contentType.subtypeName == "password")
    assert(!part2.contentType.params.contains("charset"))
  }

  it should "create part with file content" in {
    val photoFile = File("src/test/resources/photo.svg")

    val part1 = Part("photo", photoFile)
    assert(part1.name == "photo")
    assert(part1.fileName.contains("photo.svg"))
    assert(part1.getFile() == photoFile)
    assert(part1.getBytes() sameElements photoFile.getBytes())
    assert(part1.getString() == String(photoFile.getBytes(), "UTF-8"))
    assert(part1.size == photoFile.length)
    assert(part1.contentDisposition.name == "form-data")
    assert(part1.contentDisposition.params("name") == "photo")
    assert(part1.contentDisposition.params("filename") == "photo.svg")
    assert(part1.contentType.isImage)
    assert(part1.contentType.subtypeName == "svg+xml")

    val part2 = Part("photo", photoFile, Some("landscape.svg"))
    assert(part2.name == "photo")
    assert(part2.fileName.contains("landscape.svg"))
    assert(part2.getFile() == photoFile)
    assert(part2.getBytes() sameElements photoFile.getBytes())
    assert(part2.getString() == String(photoFile.getBytes(), "UTF-8"))
    assert(part2.size == photoFile.length)
    assert(part2.contentDisposition.name == "form-data")
    assert(part2.contentDisposition.params("name") == "photo")
    assert(part2.contentDisposition.params("filename") == "landscape.svg")
    assert(part2.contentType.isImage)
    assert(part2.contentType.subtypeName == "svg+xml")

    val part3 = Part("photo", photoFile, None)
    assert(part3.name == "photo")
    assert(part3.fileName.isEmpty)
    assert(part3.getFile() == photoFile)
    assert(part3.getBytes() sameElements photoFile.getBytes())
    assert(part3.getString() == String(photoFile.getBytes(), "UTF-8"))
    assert(part3.size == photoFile.length)
    assert(part3.contentDisposition.name == "form-data")
    assert(part3.contentDisposition.params("name") == "photo")
    assert(!part3.contentDisposition.params.contains("filename"))
    assert(part3.contentType.isImage)
    assert(part3.contentType.subtypeName == "svg+xml")

    val part4 = Part(DispositionType("form-data", "name" -> "photo"), MediaType("image/picture"), photoFile)
    assert(part4.name == "photo")
    assert(part4.fileName.isEmpty)
    assert(part4.getFile() == photoFile)
    assert(part4.getBytes() sameElements photoFile.getBytes())
    assert(part4.getString() == String(photoFile.getBytes(), "UTF-8"))
    assert(part4.size == photoFile.length)
    assert(part4.contentDisposition.name == "form-data")
    assert(part4.contentDisposition.params("name") == "photo")
    assert(!part4.contentDisposition.params.contains("filename"))
    assert(part4.contentType.isImage)
    assert(part4.contentType.subtypeName == "picture")

    val part5 = Part(DispositionType("form-data", "name" -> "photo", "filename" -> "image.pic"), MediaType("image/picture"), photoFile)
    assert(part5.name == "photo")
    assert(part5.fileName.contains("image.pic"))
    assert(part5.getFile() == photoFile)
    assert(part5.getBytes() sameElements photoFile.getBytes())
    assert(part5.getString() == String(photoFile.getBytes(), "UTF-8"))
    assert(part5.size == photoFile.length)
    assert(part5.contentDisposition.name == "form-data")
    assert(part5.contentDisposition.params("name") == "photo")
    assert(part5.contentDisposition.params("filename") == "image.pic")
    assert(part5.contentType.isImage)
    assert(part5.contentType.subtypeName == "picture")
  }

  it should "should not create part with invalid content disposition" in {
    val noName     = DispositionType("form-data")
    val noFormData = DispositionType("mixed", "name" -> "id")

    assertThrows[HttpException] { Part(noName, MediaType.plain, "Hello, world") }
    assertThrows[HttpException] { Part(noName, MediaType.octetStream, File("photo.svg")) }
    assertThrows[HttpException] { Part(noFormData, MediaType.plain, "Hello, world") }
    assertThrows[HttpException] { Part(noFormData, MediaType.octetStream, File("photo.svg")) }
  }

  it should "create multipart" in {
    val id = Part("id", "root")
    val photo = Part("photo", File("photo.svg"))
    val rap = Part("genre", "Rap")
    val rnb = Part("genre", "R&B")
    val reggae = Part("genre", "Reggae")
    val multipart = Multipart(id, photo, rap, rnb, reggae)

    assert(multipart.parts == Seq(id, photo, rap, rnb, reggae))

    assert(multipart.getPart("id").contains(id))
    assert(multipart.getParts("id") == Seq(id))
    assert(multipart.getString("id").contains("root"))

    assert(multipart.getPart("photo").contains(photo))
    assert(multipart.getParts("photo") == Seq(photo))
    assert(multipart.getFile("photo").contains(File("photo.svg")))

    assert(multipart.getPart("genre").contains(rap))
    assert(multipart.getParts("genre") == Seq(rap, rnb, reggae))
    assert(multipart.getString("genre").contains("Rap"))

    assert(multipart.getPart("none").isEmpty)
    assert(multipart.getParts("none").isEmpty)
    assert(multipart.getString("none").isEmpty)
    assert(multipart.getFile("none").isEmpty)

    val query = multipart.toQuery
    assert(query("id") == "root")
    assert(query.getValues("id") == Seq("root"))
    assert(query("genre") == "Rap")
    assert(query.getValues("genre") == Seq("Rap", "R&B", "Reggae"))
    assertThrows[NoSuchElementException] { query("photo") }
  }
