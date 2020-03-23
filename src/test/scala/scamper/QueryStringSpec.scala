/*
 * Copyright 2019 Carlos Conyers
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

class QueryStringSpec extends org.scalatest.flatspec.AnyFlatSpec {
  it should "create QueryString from map" in {
    val query = QueryString(Map("id" -> Seq("0"), "name" -> Seq("root"), "groups" -> Seq("wheel", "admin")))

    assert(query.contains("id"))
    assert(query("id") == "0")
    assert(query.get("id").contains("0"))
    assert(query.getValues("id") == Seq("0"))

    assert(query.contains("name"))
    assert(query("name") == "root")
    assert(query.get("name").contains("root"))
    assert(query.getValues("name") == Seq("root"))

    assert(query.contains("groups"))
    assert(query("groups") == "wheel")
    assert(query.get("groups").contains("wheel"))
    assert(query.getValues("groups") == Seq("wheel", "admin"))

    assert(!query.isEmpty)
    assert(!query.contains("idx"))

    val added = query.add("name", "superuser", "admin").add("description", "user information")
    assert(added.contains("id"))
    assert(added.contains("name"))
    assert(added("name") == "root")
    assert(added.getValues("name") == Seq("root", "superuser", "admin"))
    assert(added.contains("groups"))
    assert(added.contains("description"))
    assert(added.getValues("description") == Seq("user information"))

    val updated = query.update("name", "superuser", "admin").update("description", "user information")
    assert(updated.contains("id"))
    assert(updated.contains("name"))
    assert(updated("name") == "superuser")
    assert(updated.getValues("name") == Seq("superuser", "admin"))
    assert(updated.contains("groups"))
    assert(updated.contains("description"))
    assert(updated.getValues("description") == Seq("user information"))

    val removed = query.remove("name").remove("description")
    assert(removed.contains("id"))
    assert(!removed.contains("name"))
    assert(removed.contains("groups"))
    assert(!removed.contains("description"))

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

  it should "create QueryString from sequence of name-value pairs" in {
    val query = QueryString("id" -> "0", "name" -> "root", "groups" -> "wheel", "groups" -> "admin")

    assert(query.contains("id"))
    assert(query("id") == "0")
    assert(query.get("id").contains("0"))
    assert(query.getValues("id") == Seq("0"))

    assert(query.contains("name"))
    assert(query("name") == "root")
    assert(query.get("name").contains("root"))
    assert(query.getValues("name") == Seq("root"))

    assert(query.contains("groups"))
    assert(query("groups") == "wheel")
    assert(query.get("groups").contains("wheel"))
    assert(query.getValues("groups") == Seq("wheel", "admin"))

    assert(!query.isEmpty)
    assert(!query.contains("idx"))

    val added = query.add("name", "superuser", "admin").add("description", "user information")
    assert(added.contains("id"))
    assert(added.contains("name"))
    assert(added("name") == "root")
    assert(added.getValues("name") == Seq("root", "superuser", "admin"))
    assert(added.contains("groups"))
    assert(added.contains("description"))
    assert(added.getValues("description") == Seq("user information"))

    val updated = query.update("name", "superuser", "admin").update("description", "user information")
    assert(updated.contains("id"))
    assert(updated.contains("name"))
    assert(updated("name") == "superuser")
    assert(updated.getValues("name") == Seq("superuser", "admin"))
    assert(updated.contains("groups"))
    assert(updated.contains("description"))
    assert(updated.getValues("description") == Seq("user information"))

    val removed = query.remove("name").remove("description")
    assert(removed.contains("id"))
    assert(!removed.contains("name"))
    assert(removed.contains("groups"))
    assert(!removed.contains("description"))

    val toMap = query.toMap
    assert(toMap("id") == Seq("0"))
    assert(toMap("name") == Seq("root"))
    assert(toMap("groups") == Seq("wheel", "admin"))

    val toSimpleMap = query.toSimpleMap
    assert(toSimpleMap("id") == "0")
    assert(toSimpleMap("name") == "root")
    assert(toSimpleMap("groups") == "wheel")

    val empty = QueryString(Nil)
    assert(empty.isEmpty)
    assert(empty.toString == "")
  }

  it should "create QueryString formatted query string" in {
    val query = QueryString("id=0&name=root&groups=wheel&groups=admin")

    assert(query.contains("id"))
    assert(query("id") == "0")
    assert(query.get("id").contains("0"))
    assert(query.getValues("id") == Seq("0"))

    assert(query.contains("name"))
    assert(query("name") == "root")
    assert(query.get("name").contains("root"))
    assert(query.getValues("name") == Seq("root"))

    assert(query.contains("groups"))
    assert(query("groups") == "wheel")
    assert(query.get("groups").contains("wheel"))
    assert(query.getValues("groups") == Seq("wheel", "admin"))

    assert(!query.isEmpty)
    assert(!query.contains("idx"))

    val added = query.add("name", "superuser", "admin").add("description", "user information")
    assert(added.contains("id"))
    assert(added.contains("name"))
    assert(added("name") == "root")
    assert(added.getValues("name") == Seq("root", "superuser", "admin"))
    assert(added.contains("groups"))
    assert(added.contains("description"))
    assert(added.getValues("description") == Seq("user information"))

    val updated = query.update("name", "superuser", "admin").update("description", "user information")
    assert(updated.contains("id"))
    assert(updated.contains("name"))
    assert(updated("name") == "superuser")
    assert(updated.getValues("name") == Seq("superuser", "admin"))
    assert(updated.contains("groups"))
    assert(updated.contains("description"))
    assert(updated.getValues("description") == Seq("user information"))

    val removed = query.remove("name").remove("description")
    assert(removed.contains("id"))
    assert(!removed.contains("name"))
    assert(removed.contains("groups"))
    assert(!removed.contains("description"))

    val toMap = query.toMap
    assert(toMap("id") == Seq("0"))
    assert(toMap("name") == Seq("root"))
    assert(toMap("groups") == Seq("wheel", "admin"))

    val toSimpleMap = query.toSimpleMap
    assert(toSimpleMap("id") == "0")
    assert(toSimpleMap("name") == "root")
    assert(toSimpleMap("groups") == "wheel")

    val empty = QueryString(Nil)
    assert(empty.isEmpty)
    assert(empty.toString == "")
  }

  it should "create empty QueryString" in {
    val query = QueryString.empty

    val added = query.add("name", "superuser", "admin")
    assert(added.contains("name"))
    assert(added("name") == "superuser")
    assert(added.getValues("name") == Seq("superuser", "admin"))

    val updated = query.update("name", "superuser", "admin")
    assert(updated("name") == "superuser")
    assert(updated.getValues("name") == Seq("superuser", "admin"))

    val removed = query.remove("name")
    assert(!removed.contains("name"))

    assert(query.toSeq.isEmpty)
    assert(query.toMap.isEmpty)
    assert(query.toSimpleMap.isEmpty)
  }

  it should "get Int values from QueryString" in {
    val query = QueryString(Map("id" -> Seq("1", "2", "3"), "nan" -> Seq("a")))

    assert(query.getInt("id").contains(1))
    assert(query.getIntOrElse("id", 4) == 1)
    assert(query.getIntOrElse("idx", 4) == 4)
    assert(query.getIntValues("id") == Seq(1, 2, 3))

    assertThrows[NumberFormatException](query.getInt("nan"))
    assertThrows[NumberFormatException](query.getIntValues("nan"))
  }

  it should "get Long values from QueryString" in {
    val query = QueryString(Map("id" -> Seq("1", "2", "3"), "nan" -> Seq("a")))

    assert(query.getLong("id").contains(1))
    assert(query.getLongOrElse("id", 4) == 1)
    assert(query.getLongOrElse("idx", 4) == 4)
    assert(query.getLongValues("id") == Seq(1, 2, 3))

    assertThrows[NumberFormatException](query.getLong("nan"))
    assertThrows[NumberFormatException](query.getLongValues("nan"))
  }

  it should "throw NoSuchElementException if parameter not present" in {
    val query = QueryString("id=0&name=root&groups=wheel&groups=admin")
    assertThrows[NoSuchElementException] { query("idx") }
  }
}

