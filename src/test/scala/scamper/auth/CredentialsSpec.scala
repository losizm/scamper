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
package scamper.auth

import scamper.Base64

class CredentialsSpec extends org.scalatest.flatspec.AnyFlatSpec {
  private val token = Base64.encodeToString("guest:letmein")

  "Credentials" should "be created with basic scheme" in {
    val credentials = Credentials.parse(s"Basic $token")
    assert(credentials.scheme == "Basic")
    assert(credentials.token == token)
    assert(credentials.toString == s"Basic $token")
    assert(credentials.isInstanceOf[BasicCredentials])

    val auth = credentials.asInstanceOf[BasicCredentials]
    assert(auth.user == "guest")
    assert(auth.password == "letmein")
  }

  it should "be created with bearer scheme" in {
    val credentials = Credentials.parse(s"Bearer $token")
    assert(credentials.scheme == "Bearer")
    assert(credentials.token == token)
    assert(credentials.toString == s"Bearer $token")
    assert(credentials.isInstanceOf[BearerCredentials])

    val auth = credentials.asInstanceOf[BearerCredentials]
    assert(auth.token == token)
  }

  it should "be created with other scheme" in {
    val credentials = Credentials.parse("Insecure aXNzYTpyYWUK")
    assert(credentials.scheme == "Insecure")
    assert(credentials.token == "aXNzYTpyYWUK")
    assert(credentials.toString == "Insecure aXNzYTpyYWUK")
  }

  it should "be destructured" in {
    Credentials.parse(s"Basic $token") match {
      case BasicCredentials(user, password) =>
        assert(user == "guest")
        assert(password == "letmein")
    }

    Credentials.parse(s"Bearer $token") match {
      case BearerCredentials(token) =>
        assert(token.contains(token))
    }

    Credentials.parse("Insecure aXNzYTpyYWUK") match {
      case Credentials(scheme, token) =>
        assert(scheme == "Insecure")
        assert(token == "aXNzYTpyYWUK")
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Credentials.parse("Basic"))
    assertThrows[IllegalArgumentException](Credentials.parse("Basic realm"))
    assertThrows[IllegalArgumentException](Credentials.parse("Basic aXNzYTpyYWUK realm=Insecure"))
    assertThrows[IllegalArgumentException](Credentials.parse("Basic description=none"))
    assertThrows[IllegalArgumentException](Credentials.parse("Bearer scope=openid profile email"))
    assertThrows[IllegalArgumentException](Credentials.parse("Insecure issa:rae"))
    assertThrows[IllegalArgumentException](Credentials.parse("Insecure aXNzYTpyYWUK realm=Insecure"))
  }
}
