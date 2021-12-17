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

import scala.language.implicitConversions

class ImplicitsSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "convert (String, String) to Part" in {
    val part: Part = "user" -> "nobody"
    assert(part.name == "user")
    assert(part.getString() == "nobody")
    assert(part.fileName.isEmpty)
    assert(part.contentType.fullName == "text/plain")
  }

  it should "convert (String, Array[Byte]) to Part" in {
    val part: Part = "passwd" -> Array[Byte](0, 1, 2)
    assert(part.name == "passwd")
    assert(part.fileName.isEmpty)
    assert(part.getBytes() sameElements Array[Byte](0, 1, 2))
    assert(part.contentType.fullName == "application/octet-stream")
  }

  it should "convert (String, File) to Part" in {
    val part: Part = "passwd" -> File("test.json")
    assert(part.name == "passwd")
    assert(part.getFile() == File("test.json"))
    assert(part.fileName.contains("test.json"))
    assert(part.contentType.fullName == "application/json")
  }
