/*
 * Copyright 2017-2020 Carlos Conyers
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

import scamper.Implicits.{ stringToEntity, stringToHeader, stringToUri }
import scamper.ResponseStatus.Registry._
import scamper.headers._

class HttpResponseSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "create with location" in {
    val res = SeeOther().withLocation("/find")
    assert(res.status == SeeOther)
    assert(res.location.toString == "/find")
  }

  it should "create with attributes" in {
    var res = Ok().withAttributes("id" -> 1, "name" -> "response").withAttribute("success" -> true)
    assert(res.status == Ok)
    assert(res.attributes.size == 3)
    assert(res.getAttribute[Int]("id").contains(1))
    assert(res.getAttribute[String]("name").contains("response"))
    assert(res.getAttributeOrElse("success", false))
    assert(res.getAttributeOrElse("answer", 45) == 45)
    assert(res.withAttributes(Map.empty[String, Any]).attributes.size == 0)

    res = res.removeAttributes("name")
    assert(res.attributes.size == 2)
    assert(res.attributes.contains("id"))
    assert(res.attributes.contains("success"))
    assert(!res.attributes.contains("name"))
  }

  it should "get default value if header not found" in {
    val server = Header("Server", "Scamper/x.x")
    val location = Header("Location", "/find")
    val res = SeeOther().withHeader(server)

    assert(res.getHeaderOrElse("Server", throw HeaderNotFound("Server")) == server)
    assert(res.getHeaderValueOrElse("Server", throw HeaderNotFound("Server")) == server.value)

    assert(res.getHeaderOrElse("Location", location) == location)
    assert(res.getHeaderValueOrElse("Location", location.value) == location.value)
  }

  it should "throw exception if header not found" in {
    val res = SeeOther()
    assertThrows[HeaderNotFound](res.getHeaderOrElse("Server", throw HeaderNotFound("Server")))
    assertThrows[HeaderNotFound](res.getHeaderValueOrElse("Server", throw HeaderNotFound("Server")))
  }
}
