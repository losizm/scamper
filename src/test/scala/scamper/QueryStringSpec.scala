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

import org.scalatest.FlatSpec

class QueryStringSpec extends FlatSpec {
  "QueryString" should "be created from map" in {
    val query = QueryString(Map("id" -> Seq("0"), "name" -> Seq("root"), "groups" -> Seq("wheel", "admin")))

    assert(query("id") == "0")
    assert(query.getValue("id").contains("0"))
    assert(query.getValues("id") == Seq("0"))

    assert(query("name") == "root")
    assert(query.getValue("name").contains("root"))
    assert(query.getValues("name") == Seq("root"))

    assert(query("groups") == "wheel")
    assert(query.getValue("groups").contains("wheel"))
    assert(query.getValues("groups") == Seq("wheel", "admin"))

    assert(!query.isEmpty)

    val toMap = query.toMap
    assert(toMap("id") == Seq("0"))
    assert(toMap("name") == Seq("root"))
    assert(toMap("groups") == Seq("wheel", "admin"))

    val toSimpleMap = query.toSimpleMap
    assert(toSimpleMap("id") == "0")
    assert(toSimpleMap("name") == "root")
    assert(toSimpleMap("groups") == "wheel")

    val empty = QueryString(Map.empty[String, Seq[String]])
    assert(empty.isEmpty)
    assert(empty.toString == "")
  }

  it should "be created from name/value pairs" in {
    val query = QueryString("id" -> "0", "name" -> "root", "groups" -> "wheel", "groups" -> "admin")

    assert(query("id") == "0")
    assert(query.getValue("id").contains("0"))
    assert(query.getValues("id") == Seq("0"))

    assert(query("name") == "root")
    assert(query.getValue("name").contains("root"))
    assert(query.getValues("name") == Seq("root"))

    assert(query("groups") == "wheel")
    assert(query.getValue("groups").contains("wheel"))
    assert(query.getValues("groups") == Seq("wheel", "admin"))

    assert(!query.isEmpty)

    val toMap = query.toMap
    assert(toMap("id") == Seq("0"))
    assert(toMap("name") == Seq("root"))
    assert(toMap("groups") == Seq("wheel", "admin"))

    val toSimpleMap = query.toSimpleMap
    assert(toSimpleMap("id") == "0")
    assert(toSimpleMap("name") == "root")
    assert(toSimpleMap("groups") == "wheel")

    val empty = QueryString()
    assert(empty.isEmpty)
    assert(empty.toString == "")
  }

  it should "be created from formatted query string" in {
    val query = QueryString("id=0&name=root&groups=wheel&groups=admin")

    assert(query("id") == "0")
    assert(query.getValue("id").contains("0"))
    assert(query.getValues("id") == Seq("0"))

    assert(query("name") == "root")
    assert(query.getValue("name").contains("root"))
    assert(query.getValues("name") == Seq("root"))

    assert(query("groups") == "wheel")
    assert(query.getValue("groups").contains("wheel"))
    assert(query.getValues("groups") == Seq("wheel", "admin"))

    assert(!query.isEmpty)

    val toMap = query.toMap
    assert(toMap("id") == Seq("0"))
    assert(toMap("name") == Seq("root"))
    assert(toMap("groups") == Seq("wheel", "admin"))

    val toSimpleMap = query.toSimpleMap
    assert(toSimpleMap("id") == "0")
    assert(toSimpleMap("name") == "root")
    assert(toSimpleMap("groups") == "wheel")

    val empty = QueryString()
    assert(empty.isEmpty)
    assert(empty.toString == "")
  }
}

