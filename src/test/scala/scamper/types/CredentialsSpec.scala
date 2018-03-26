package scamper.types

import java.util.Base64
import org.scalatest.FlatSpec

class CredentialsSpec extends FlatSpec {
  "Credentials" should "be created without token and params" in {
    val credentials = Credentials.parse("Basic")
    assert(credentials.scheme == "Basic")
    assert(!credentials.token.isDefined)
    assert(credentials.params.isEmpty)
    assert(credentials.toString == "Basic")
  }

  it should "be created with token and no params" in {
    val token = Base64.getEncoder().encodeToString("admin:secr,t".getBytes)
    val credentials = Credentials.parse(s"Basic $token")
    assert(credentials.scheme == "Basic")
    assert(credentials.token.contains(token))
    assert(credentials.params.isEmpty)
    assert(credentials.toString == s"Basic $token")
  }

  it should "be created with params and no token" in {
    val credentials = Credentials.parse("Basic user=admin, password=\"secr,t\"")
    assert(credentials.scheme == "Basic")
    assert(!credentials.token.isDefined)
    assert(credentials.params("user") == "admin")
    assert(credentials.params("password") == "secr,t")
    assert(credentials.toString == "Basic user=admin, password=\"secr,t\"")
  }

  it should "be destructured" in {
    Credentials.parse("Basic") match {
      case Credentials(scheme, token, params) =>
        assert(scheme == "Basic")
        assert(!token.isDefined)
        assert(params.isEmpty)
    }

    Credentials.parse("Basic admin$secret") match {
      case Credentials(scheme, Some(token), params) =>
        assert(scheme == "Basic")
        assert(token.contains("admin$secret"))
        assert(params.isEmpty)
    }

    Credentials.parse("Basic user=admin, password=\"secr,t\"") match {
      case Credentials(scheme, token, params) =>
        assert(scheme == "Basic")
        assert(!token.isDefined)
        assert(params("user") == "admin")
        assert(params("password") == "secr,t")
    }
  }

  it should "not be created with malformed value" in {
    assertThrows[IllegalArgumentException](Credentials.parse("Basic /"))
    assertThrows[IllegalArgumentException](Credentials.parse("Basic ="))
    assertThrows[IllegalArgumentException](Credentials.parse("Basic =secret"))
  }
}

