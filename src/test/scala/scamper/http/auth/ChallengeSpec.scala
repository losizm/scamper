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
package auth

class ChallengeSpec extends org.scalatest.flatspec.AnyFlatSpec:
  "Challenge" should "be created with basic scheme" in {
    val challenge = Challenge.parse("Basic realm=\"Admin Console\", charset=utf-8")
    assert(challenge.scheme == "Basic")
    assert(challenge.params("realm") == "Admin Console")
    assert(challenge.params("charset") == "utf-8")
    assert(challenge.toString == "Basic realm=\"Admin Console\", charset=utf-8")
    assert(challenge.isInstanceOf[BasicChallenge])

    val auth = challenge.asInstanceOf[BasicChallenge]
    assert(auth.realm == "Admin Console")
  }

  it should "be created with bearer scheme" in {
    val challenge = Challenge.parse("Bearer realm=example, error=invalid_token, scope=\"user profile\"")
    assert(challenge.scheme == "Bearer")
    assert(challenge.params("realm") == "example")
    assert(challenge.params("error") == "invalid_token")
    assert(challenge.toString == "Bearer realm=\"example\", error=invalid_token, scope=\"user profile\"")
    assert(challenge.isInstanceOf[BearerChallenge])

    val auth = challenge.asInstanceOf[BearerChallenge]
    assert(auth.realm.contains("example"))
    assert(auth.error.contains("invalid_token"))
    assert(auth.scope.size == 2)
    assert(auth.scope == Seq("user", "profile"))
    assert(!auth.isInvalidRequest)
    assert(auth.isInvalidToken)
    assert(!auth.isInsufficientScope)
  }

  it should "be created with other scheme" in {
    val challenge = Challenge.parse(s"Insecure foo=bar")
    assert(challenge.scheme == "Insecure")
    assert(challenge.params.size == 1)
    assert(challenge.params("foo") == "bar")
    assert(challenge.toString == s"Insecure foo=bar")
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Challenge.parse("Basic"))
    assertThrows[IllegalArgumentException](Challenge.parse("Basic realm"))
    assertThrows[IllegalArgumentException](Challenge.parse("Basic aXNzYTpyYWUK realm=Insecure"))
    assertThrows[IllegalArgumentException](Challenge.parse("Basic description=none"))
    assertThrows[IllegalArgumentException](Challenge.parse("Bearer scope=openid profile email"))
    assertThrows[IllegalArgumentException](Challenge.parse("Insecure issa:rae"))
    assertThrows[IllegalArgumentException](Challenge.parse("Insecure aXNzYTpyYWUK realm=Insecure"))
  }
