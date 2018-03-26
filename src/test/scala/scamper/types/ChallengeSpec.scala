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

