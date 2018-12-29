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

class ChallengeSpec extends FlatSpec {
  "Challenge" should "be created with Basic authentication" in {
    val challenge = Challenge.parse("Basic realm=\"User Workshop\", charset=utf-8")
    assert(challenge.scheme == "Basic")
    assert(!challenge.token.isDefined)
    assert(challenge.params("realm") == "User Workshop")
    assert(challenge.params("charset") == "utf-8")
    assert(challenge.toString == "Basic realm=\"User Workshop\", charset=utf-8")
    assert(challenge.isInstanceOf[BasicAuthentication])

    val auth = challenge.asInstanceOf[BasicAuthentication]
    assert(auth.realm == "User Workshop")
  }

  it should "be created with token" in {
    val challenge = Challenge.parse(s"Insecure user-workshop")
    assert(challenge.scheme == "Insecure")
    assert(challenge.token.contains("user-workshop"))
    assert(challenge.params.isEmpty)
    assert(challenge.toString == s"Insecure user-workshop")
  }

  it should "be created with parameters" in {
    val challenge = Challenge.parse("Insecure realm=\"Admin Console\", description=none")
    assert(challenge.scheme == "Insecure")
    assert(!challenge.token.isDefined)
    assert(challenge.params("realm") == "Admin Console")
    assert(challenge.params("description") == "none")
    assert(challenge.toString == "Insecure realm=\"Admin Console\", description=none")
  }

  it should "be destructured" in {
    Challenge.parse("Basic realm=Workshop, charset=utf-8") match {
      case BasicAuthentication(realm, params) =>
        assert(realm == "Workshop")
        assert(params("realm") == "Workshop")
        assert(params("charset") == "utf-8")
    }

    Challenge.parse("Insecure realm=\"Admin Console\", description=none") match {
      case Challenge(scheme, token, params) =>
        assert(scheme == "Insecure")
        assert(!token.isDefined)
        assert(params("realm") == "Admin Console")
        assert(params("description") == "none")
    }

    Challenge.parse(s"Insecure user-workshop") match {
      case Challenge(scheme, Some(token), params) =>
        assert(scheme == "Insecure")
        assert(token.contains("user-workshop"))
        assert(params.isEmpty)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Challenge.parse("Basic realm"))
    assertThrows[IllegalArgumentException](Challenge.parse("Basic description=none"))
    assertThrows[IllegalArgumentException](Challenge.parse("Insecure ="))
  }
}
