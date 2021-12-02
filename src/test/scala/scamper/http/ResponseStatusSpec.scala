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

import java.io.{ File, FileInputStream }

import ResponseStatus.Registry.*

class ResponseStatusSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "get registered status" in {
    assert { ResponseStatus(100) == Continue }
    assert { ResponseStatus.get(100).contains(Continue) }

    assert { ResponseStatus(200) == Ok }
    assert { ResponseStatus.get(200).contains(Ok) }

    assert { ResponseStatus(300) == MultipleChoices }
    assert { ResponseStatus.get(300).contains(MultipleChoices) }

    assert { ResponseStatus(400) == BadRequest }
    assert { ResponseStatus.get(400).contains(BadRequest) }

    assert { ResponseStatus(500) == InternalServerError }
    assert { ResponseStatus.get(500).contains(InternalServerError) }
  }

  it should "not get registered status" in {
    assertThrows[NoSuchElementException] { ResponseStatus(600) }
    assert { ResponseStatus.get(600).isEmpty }
  }

  it should "create response with message body" in {
    val res1 = NotFound("Not Found".getBytes("UTF-8"))
    assert(res1.status == NotFound)
    assert(res1.body.getBytes().sameElements("Not Found".getBytes("UTF-8")))

    val res2 = BadRequest("Bad Request")
    assert(res2.status == BadRequest)
    assert(res2.body.getBytes().sameElements("Bad Request".getBytes("UTF-8")))

    val res3 = Ok(File("src/test/resources/test.html"))
    assert(res3.status == Ok)
    assert(res3.body.getBytes().sameElements(Entity(File("src/test/resources/test.html")).getBytes()))

    val res4 = Ok(FileInputStream("src/test/resources/test.html"))
    assert(res4.status == Ok)
    assert(res4.body.getBytes().sameElements(Entity(FileInputStream("src/test/resources/test.html")).getBytes()))
  }
