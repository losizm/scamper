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
package server

class MountPathSpec extends org.scalatest.flatspec.AnyFlatSpec:
  it should "match mount path to another path" in {
    val p1 = MountPath("/")
    assert(p1.matches("/"))
    assert(p1.matches("/abc"))
    assert(p1.matches("/abc/"))
    assert(p1.matches("/abc/xyz"))
    assert(p1.matches("/abc/xyz/"))
    assert(p1.matches("/abc/xyz/123"))
    assert(p1.matches("/abc/xyz/123/"))
    assert(p1.matches("/abcxyz"))
    assert(p1.matches("/xyz/abc"))

    val p2 = MountPath("/abc")
    assert(!p2.matches("/"))
    assert(p2.matches("/abc"))
    assert(p2.matches("/abc/"))
    assert(p2.matches("/abc/xyz"))
    assert(p2.matches("/abc/xyz/"))
    assert(p2.matches("/abc/xyz/123"))
    assert(p2.matches("/abc/xyz/123/"))
    assert(!p2.matches("/abcxyz"))
    assert(!p2.matches("/xyz/abc"))

    val p3 = MountPath("/abc/xyz")
    assert(!p3.matches("/"))
    assert(!p3.matches("/abc"))
    assert(!p3.matches("/abc/"))
    assert(p3.matches("/abc/xyz"))
    assert(p3.matches("/abc/xyz/"))
    assert(p3.matches("/abc/xyz/123"))
    assert(p3.matches("/abc/xyz/123/"))
    assert(!p3.matches("/abcxyz"))
    assert(!p3.matches("/xyz/abc"))
  }

  it should "not create invalid mount path" in {
    assertThrows[IllegalArgumentException](MountPath(""))
    assertThrows[IllegalArgumentException](MountPath("abc/xyz"))
    assertThrows[IllegalArgumentException](MountPath("/:abc"))
    assertThrows[IllegalArgumentException](MountPath("/*abc"))
    assertThrows[IllegalArgumentException](MountPath("/:abc/xyz"))
    assertThrows[IllegalArgumentException](MountPath("/*abc/xyz"))
    assertThrows[IllegalArgumentException](MountPath("/abc/:xyz"))
    assertThrows[IllegalArgumentException](MountPath("/abc/*xyz"))
    assertThrows[IllegalArgumentException](MountPath("/.."))
    assertThrows[IllegalArgumentException](MountPath("/../abc/xyz"))
  }
