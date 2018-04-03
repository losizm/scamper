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

import java.util.Base64
import org.scalatest.FlatSpec

class ChallengeSpec extends FlatSpec {
  "Challenge" should "be created without token and params" in {
    val challenge = Challenge.parse("Basic")
    assert(challenge.scheme == "Basic")
    assert(!challenge.token.isDefined)
    assert(challenge.params.isEmpty)
    assert(challenge.toString == "Basic")
  }

  it should "be created with token and no params" in {
    val token = Base64.getEncoder().encodeToString("realm=xyz".getBytes)
    val challenge = Challenge.parse(s"Basic $token")
    assert(challenge.scheme == "Basic")
    assert(challenge.token.contains(token))
    assert(challenge.params.isEmpty)
    assert(challenge.toString == s"Basic $token")
  }

  it should "be created with params and no token" in {
    val challenge = Challenge.parse("Basic realm=\"Admin Console\", description=none")
    assert(challenge.scheme == "Basic")
    assert(!challenge.token.isDefined)
    assert(challenge.params("realm") == "Admin Console")
    assert(challenge.params("description") == "none")
    assert(challenge.toString == "Basic realm=\"Admin Console\", description=none")
  }

  it should "be destructured" in {
    Challenge.parse("Basic") match {
      case Challenge(scheme, token, params) =>
        assert(scheme == "Basic")
        assert(!token.isDefined)
        assert(params.isEmpty)
    }

    Challenge.parse("Basic realm=\"Admin Console\", description=none") match {
      case Challenge(scheme, token, params) =>
        assert(scheme == "Basic")
        assert(!token.isDefined)
        assert(params("realm") == "Admin Console")
        assert(params("description") == "none")
    }

    Challenge.parse("Basic admin$secret") match {
      case Challenge(scheme, Some(token), params) =>
        assert(scheme == "Basic")
        assert(token.contains("admin$secret"))
        assert(params.isEmpty)
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Challenge.parse("Basic /"))
    assertThrows[IllegalArgumentException](Challenge.parse("Basic ="))
    assertThrows[IllegalArgumentException](Challenge.parse("Basic =secret"))
  }
}
