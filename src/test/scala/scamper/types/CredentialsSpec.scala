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
package scamper.types

import org.scalatest.FlatSpec

import scamper.Base64

class CredentialsSpec extends FlatSpec {
  private val token = Base64.encodeToString("guest:letmein")

  "Credentials" should "be created with Basic authorization" in {
    val credentials = Credentials.parse(s"Basic $token")
    assert(credentials.scheme == "Basic")
    assert(credentials.token.contains(token))
    assert(credentials.params.isEmpty)
    assert(credentials.toString == s"Basic $token")
    assert(credentials.isInstanceOf[BasicAuthorization])

    val auth = credentials.asInstanceOf[BasicAuthorization]
    assert(auth.user == "guest")
    assert(auth.password == "letmein")
  }

  it should "be created with token" in {
    val credentials = Credentials.parse(s"Bearer $token")
    assert(credentials.scheme == "Bearer")
    assert(credentials.token.contains(token))
    assert(credentials.params.isEmpty)
    assert(credentials.toString == s"Bearer $token")
    assert(!credentials.isInstanceOf[BasicAuthorization])
  }

  it should "be created with parameters" in {
    val credentials = Credentials.parse("Insecure user=issa, password=\"secr,t\"")
    assert(credentials.scheme == "Insecure")
    assert(!credentials.token.isDefined)
    assert(credentials.params("user") == "issa")
    assert(credentials.params("password") == "secr,t")
    assert(credentials.toString == "Insecure user=issa, password=\"secr,t\"")
  }

  it should "be destructured" in {
    Credentials.parse(s"Basic $token") match {
      case Credentials(scheme, token, params) =>
        assert(scheme == "Basic")
        assert(token.isDefined)
        assert(params.isEmpty)
    }

    Credentials.parse(s"Basic $token") match {
      case BasicAuthorization(user, password) =>
        assert(user == "guest")
        assert(password == "letmein")
    }

    Credentials.parse("Insecure user=issa, password=\"secr,t\"") match {
      case Credentials(scheme, token, params) =>
        assert(scheme == "Insecure")
        assert(!token.isDefined)
        assert(params("user") == "issa")
        assert(params("password") == "secr,t")
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Credentials.parse("Basic"))
    assertThrows[IllegalArgumentException](Credentials.parse("Basic guest:letmein"))
    assertThrows[IllegalArgumentException](Credentials.parse("Basic realm=Public"))
    assertThrows[IllegalArgumentException](Credentials.parse("Bearer /"))
    assertThrows[IllegalArgumentException](Credentials.parse("Insecure ="))
    assertThrows[IllegalArgumentException](Credentials.parse("Insecure =secret"))
  }
}
