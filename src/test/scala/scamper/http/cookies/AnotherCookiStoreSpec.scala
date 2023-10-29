/*
 * Copyright 2023 Carlos Conyers
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
package cookies

import java.time.Instant
import Instant.now as Now

import scala.language.implicitConversions

class AnotherCookieStoreSpec extends org.scalatest.flatspec.AnyFlatSpec:
  val c1 = PersistentCookieImpl(
    name = "example-1",
    value = "c063405b-c3b4-429e-b024-3edc6cca18db",
    domain = "example.com",
    path = "/",
    secureOnly = true,
    httpOnly = false,
    hostOnly = false,
    persistent = true,
    creation = Now().minusMillis(9999),
    expiry = Now().plusMillis(30000)
  )

  val c2 = PersistentCookieImpl(
    name = "example-2",
    value = "a4a91d6d-f1f9-4ae5-b987-f662a460006e",
    domain = "example.com",
    path = "/",
    secureOnly = false,
    httpOnly = false,
    hostOnly = false,
    persistent = true,
    creation = Now().minusMillis(99999),
    expiry = Now().plusMillis(20000)
  )

  val c3 = PersistentCookieImpl(
    name = "example-3",
    value = "8e6354f8-b0c9-4fdc-b813-71571e423cf7",
    domain = "example.com",
    path = "/",
    secureOnly = true,
    httpOnly = false,
    hostOnly = false,
    persistent = true,
    creation = Now().minusMillis(999999),
    expiry = Now().minusMillis(1000)
  )

  val c4 = PersistentCookieImpl(
    name = "example-4",
    value = "69480cec-4ddc-4104-9198-1cde7a0ded80",
    domain = "example.com",
    path = "/",
    secureOnly = false,
    httpOnly = false,
    hostOnly = false,
    persistent = true,
    creation = Now().minusMillis(9999999),
    expiry = Now().plusMillis(30000)
  )

  val c5 = PersistentCookieImpl(
    name = "example-5",
    value = "171eeeab-9da8-4c9a-9db7-10572a3afd5e",
    domain = "example.com",
    path = "/",
    secureOnly = true,
    httpOnly = false,
    hostOnly = false,
    persistent = false,
    creation = Now().minusMillis(99999999),
    expiry = Now().plusMillis(30000)
  )

  it should "get cookies with matching host" in {
    val cookieStore = CookieStore(Seq(c1, c2, c3, c4, c5))

    cookieStore.list.foreach(c => info(s"in -> $c"))
    assert(cookieStore.size == 5)
    
    val cookies = cookieStore.get("https://www.example.com")
    cookies.foreach(c => info(s"out -> $c"))
    assert(cookies.size == 4)
    assert(cookies.map(_.name).toSet == Set("example-1", "example-2", "example-4", "example-5"))
  }
