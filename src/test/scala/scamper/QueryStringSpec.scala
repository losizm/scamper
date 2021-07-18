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

class QueryStringSpec extends org.scalatest.flatspec.AnyFlatSpec:
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

    val updated = query.put("name", "superuser", "admin").put("description", "user information")
    assert(updated.contains("id"))
    assert(updated.contains("name"))
    assert(updated("name") == "superuser")
    assert(updated.getValues("name") == Seq("superuser", "admin"))
    assert(updated.contains("groups"))
    assert(updated.contains("description"))
    assert(updated.getValues("description") == Seq("user information"))

    val removed = query.remove("name", "description")
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

    val updated = query.put("name", "superuser", "admin").put("description", "user information")
    assert(updated.contains("id"))
    assert(updated.contains("name"))
    assert(updated("name") == "superuser")
    assert(updated.getValues("name") == Seq("superuser", "admin"))
    assert(updated.contains("groups"))
    assert(updated.contains("description"))
    assert(updated.getValues("description") == Seq("user information"))

    val removed = query.remove("name", "description")
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

    val updated = query.put("name", "superuser", "admin").put("description", "user information")
    assert(updated.contains("id"))
    assert(updated.contains("name"))
    assert(updated("name") == "superuser")
    assert(updated.getValues("name") == Seq("superuser", "admin"))
    assert(updated.contains("groups"))
    assert(updated.contains("description"))
    assert(updated.getValues("description") == Seq("user information"))

    val removed = query.remove("name", "description")
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

    val updated = query.put("name", "superuser", "admin")
    assert(updated("name") == "superuser")
    assert(updated.getValues("name") == Seq("superuser", "admin"))

    val removed = query.remove("name")
    assert(!removed.contains("name"))

    assert(query.toSeq.isEmpty)
    assert(query.toMap.isEmpty)
    assert(query.toSimpleMap.isEmpty)
  }

  it should "concatenate query strings" in {
    val toMap = Map("a" -> Seq("1"), "b" -> Seq("2", "3"))
    val toSeq = Seq("b" -> "4", "b" -> "5", "c" -> "6")

    val q1 = QueryString(toMap)
    val q2 = QueryString(toSeq)
    val q3 = q1 ++ q2
    val q4 = q2 ++ q1
    val q5 = q1 ++ toSeq
    val q6 = q2 ++ toMap

    assert(q3("a") == "1")
    assert(q3.getValues("a") == Seq("1"))
    assert(q3("b") == "2")
    assert(q3.getValues("b") == Seq("2", "3", "4", "5"))
    assert(q3("c") == "6")
    assert(q3.getValues("c") == Seq("6"))

    assert(q4("a") == "1")
    assert(q4.getValues("a") == Seq("1"))
    assert(q4("b") == "4")
    assert(q4.getValues("b") == Seq("4", "5", "2", "3"))
    assert(q4("c") == "6")
    assert(q4.getValues("c") == Seq("6"))

    assert(q5("a") == "1")
    assert(q5.getValues("a") == Seq("1"))
    assert(q5("b") == "2")
    assert(q5.getValues("b") == Seq("2", "3", "4", "5"))
    assert(q5("c") == "6")
    assert(q5.getValues("c") == Seq("6"))

    assert(q6("a") == "1")
    assert(q6.getValues("a") == Seq("1"))
    assert(q6("b") == "4")
    assert(q6.getValues("b") == Seq("4", "5", "2", "3"))
    assert(q6("c") == "6")
    assert(q6.getValues("c") == Seq("6"))

    assert(q1 ++ QueryString.empty == q1)
    assert(QueryString.empty ++ q1 == q1)
    assert(QueryString.empty ++ QueryString.empty == QueryString.empty)
  }

  it should "merge query strings" in {
    val toMap = Map("a" -> Seq("1"), "b" -> Seq("2", "3"))
    val toSeq = Seq("b" -> "4", "b" -> "5", "c" -> "6")

    val q1 = QueryString(toMap)
    val q2 = QueryString(toSeq)
    val q3 = q1 << q2
    val q4 = q2 << q1
    val q5 = q1 << toSeq
    val q6 = q2 << toMap

    assert(q3("a") == "1")
    assert(q3.getValues("a") == Seq("1"))
    assert(q3("b") == "4")
    assert(q3.getValues("b") == Seq("4", "5"))
    assert(q3("c") == "6")
    assert(q3.getValues("c") == Seq("6"))

    assert(q4("a") == "1")
    assert(q4.getValues("a") == Seq("1"))
    assert(q4("b") == "2")
    assert(q4.getValues("b") == Seq("2", "3"))
    assert(q4("c") == "6")
    assert(q4.getValues("c") == Seq("6"))

    assert(q5("a") == "1")
    assert(q5.getValues("a") == Seq("1"))
    assert(q5("b") == "4")
    assert(q5.getValues("b") == Seq("4", "5"))
    assert(q5("c") == "6")
    assert(q5.getValues("c") == Seq("6"))

    assert(q6("a") == "1")
    assert(q6.getValues("a") == Seq("1"))
    assert(q6("b") == "2")
    assert(q6.getValues("b") == Seq("2", "3"))
    assert(q6("c") == "6")
    assert(q6.getValues("c") == Seq("6"))

    assert(q1 << QueryString.empty == q1)
    assert(QueryString.empty << q1 == q1)
    assert(QueryString.empty << QueryString.empty == QueryString.empty)
  }

  it should "filter query strings" in {
    val q1 = QueryString(
      "a" -> "1",
      "b" -> "2",
      "b" -> "3",
      "b" -> "4",
      "c" -> "5",
      "c" -> "6"
    )

    val q2 = q1.filter { case (_, value) => value.toInt % 2 == 0 }
    assert(q2.getValues("a").isEmpty)
    assert(q2.getValues("b") == Seq("2", "4"))
    assert(q2.getValues("c") == Seq("6"))

    val q3 = q1.retain("a", "c")
    assert(q3.getValues("a") == Seq("1"))
    assert(q3.getValues("b").isEmpty)
    assert(q3.getValues("c") == Seq("5", "6"))

    val q4 = q1.filterNot { case (_, value) => value.toInt % 2 == 0 }
    assert(q4.getValues("a") == Seq("1"))
    assert(q4.getValues("b") == Seq("3"))
    assert(q4.getValues("c") == Seq("5"))
  }

  it should "get Int values from QueryString" in {
    val query = QueryString(Map("id" -> Seq("1", "2", "3"), "nan" -> Seq("a")))

    assert(query.getInt("id").contains(1))
    assertThrows[NumberFormatException](query.getInt("nan"))
  }

  it should "get Long values from QueryString" in {
    val query = QueryString(Map("id" -> Seq("1", "2", "3"), "nan" -> Seq("a")))

    assert(query.getLong("id").contains(1))
    assertThrows[NumberFormatException](query.getLong("nan"))
  }

  it should "throw NoSuchElementException if parameter not present" in {
    val query = QueryString("id=0&name=root&groups=wheel&groups=admin")
    assertThrows[NoSuchElementException] { query("idx") }
  }
