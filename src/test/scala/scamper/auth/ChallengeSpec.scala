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
package scamper.auth

import org.scalatest.FlatSpec

import scamper.Base64

class ChallengeSpec extends FlatSpec {
  "Challenge" should "be created with Basic authentication" in {
    val challenge = Challenge.parse("Basic realm=\"Admin Console\", charset=utf-8")
    assert(challenge.scheme == "Basic")
    assert(!challenge.token.isDefined)
    assert(challenge.params("realm") == "Admin Console")
    assert(challenge.params("charset") == "utf-8")
    assert(challenge.toString == "Basic realm=\"Admin Console\", charset=utf-8")
    assert(challenge.isInstanceOf[BasicAuthentication])

    val auth = challenge.asInstanceOf[BasicAuthentication]
    assert(auth.realm == "Admin Console")
  }

  it should "be created with token" in {
    val challenge = Challenge.parse(s"Insecure aXNzYTpyYWUK")
    assert(challenge.scheme == "Insecure")
    assert(challenge.token.contains("aXNzYTpyYWUK"))
    assert(challenge.params.isEmpty)
    assert(challenge.toString == s"Insecure aXNzYTpyYWUK")
  }

  it should "be created with parameters" in {
    val challenge = Challenge.parse("Bearer realm=\"example\", error=invalid_token")
    assert(challenge.scheme == "Bearer")
    assert(!challenge.token.isDefined)
    assert(challenge.params("realm") == "example")
    assert(challenge.params("error") == "invalid_token")
    assert(challenge.toString == "Bearer realm=example, error=invalid_token")
  }

  it should "be destructured" in {
    Challenge.parse("Basic realm=Console, charset=utf-8") match {
      case BasicAuthentication(realm, params) =>
        assert(realm == "Console")
        assert(params("realm") == "Console")
        assert(params("charset") == "utf-8")
    }

    Challenge.parse(s"Insecure aXNzYTpyYWUK") match {
      case Challenge(scheme, Some(token), params) =>
        assert(scheme == "Insecure")
        assert(token.contains("aXNzYTpyYWUK"))
        assert(params.isEmpty)
    }

    Challenge.parse("Bearer realm=\"example\", error=invalid_token") match {
      case Challenge(scheme, token, params) =>
        assert(scheme == "Bearer")
        assert(!token.isDefined)
        assert(params("realm") == "example")
        assert(params("error") == "invalid_token")
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Challenge.parse("Basic"))
    assertThrows[IllegalArgumentException](Challenge.parse("Basic realm"))
    assertThrows[IllegalArgumentException](Challenge.parse("Basic description=none"))
    assertThrows[IllegalArgumentException](Challenge.parse("Bearer user=guest&password=letmein"))
    assertThrows[IllegalArgumentException](Challenge.parse("Insecure issa:rae"))
  }
}
