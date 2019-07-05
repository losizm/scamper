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
package scamper.auth

import org.scalatest.FlatSpec

import scamper.Base64

class ChallengeSpec extends FlatSpec {
  "Challenge" should "be created with basic scheme" in {
    val challenge = Challenge.parse("Basic realm=\"Admin Console\", charset=utf-8")
    assert(challenge.scheme == "Basic")
    assert(!challenge.token.isDefined)
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
    assert(!challenge.token.isDefined)
    assert(challenge.params("realm") == "example")
    assert(challenge.params("error") == "invalid_token")
    assert { Seq("Bearer realm=\"example\", error=invalid_token, scope=\"user profile\"", "Bearer realm=\"example\", scope=\"user profile\", error=invalid_token").contains(challenge.toString) }
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

  it should "be created with token" in {
    val challenge = Challenge.parse(s"Insecure aXNzYTpyYWUK")
    assert(challenge.scheme == "Insecure")
    assert(challenge.token.contains("aXNzYTpyYWUK"))
    assert(challenge.params.isEmpty)
    assert(challenge.toString == s"Insecure aXNzYTpyYWUK")
  }

  it should "be destructured" in {
    Challenge.parse("Basic realm=Console, charset=utf-8") match {
      case BasicChallenge(realm, params) =>
        assert(realm == "Console")
        assert(params.size == 2)
        assert(params("realm") == "Console")
        assert(params("charset") == "utf-8")
    }

    Challenge.parse("Bearer realm=\"example\", error=invalid_token, scope=\"user profile\"") match {
      case BearerChallenge(Some(realm), scope, Some(error), params) =>
        assert(realm == "example")
        assert(scope == Seq("user", "profile"))
        assert(error == "invalid_token")
        assert(params.size == 3)
        assert(params("realm") == "example")
        assert(params("scope") == "user profile")
        assert(params("error") == "invalid_token")
    }

    Challenge.parse(s"Insecure aXNzYTpyYWUK") match {
      case Challenge(scheme, Some(token), params) =>
        assert(scheme == "Insecure")
        assert(token.contains("aXNzYTpyYWUK"))
        assert(params.isEmpty)
    }
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
}
