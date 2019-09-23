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

import java.net.URI

import scala.util.Try

import scamper.Base64
import AuthTypeHelper._

/** Base type for authentication header types. */
trait AuthType {
  /** Gets scheme. */
  def scheme: String
}

/**
 * Standardized type for WWW-Authenticate and Proxy-Authenticate header value.
 *
 * @see [[WwwAuthenticate]]
 * @see [[ProxyAuthenticate]]
 * @see [[Credentials]]
 */
trait Challenge extends AuthType {
  /** Gets parameters. */
  def params: Map[String, String]

  /** Returns formatted challenge. */
  override lazy val toString: String =
    scheme + AuthParams.format(params)
}

/** Challenge for Basic authentication. */
trait BasicChallenge extends Challenge {
  val scheme: String = "Basic"

  /** Gets realm. */
  def realm: String
}

/** Factory for BasicChallenge. */
object BasicChallenge {
  /** Creates BasicChallenge with supplied credentials. */
  def apply(realm: String, params: (String, String)*): BasicChallenge =
    BasicChallengeImpl(realm, Params(("realm" -> realm) +: params))

  /** Destructures BasicChallenge. */
  def unapply(auth: BasicChallenge): Option[(String, Map[String, String])] =
    Some(auth.realm -> auth.params)
}

private case class BasicChallengeImpl(realm: String, params: Map[String, String]) extends BasicChallenge

/** Challenge for Bearer authentication. */
trait BearerChallenge extends Challenge {
  val scheme: String = "Bearer"

  /** Gets realm. */
  def realm: Option[String]

  /** Gets scope. */
  def scope: Seq[String]

  /** Gets `error` parameter. */
  def error: Option[String]

  /** Gets `error_description` parameter. */
  def errorDescription: Option[String]

  /** Gets `error_uri` parameter. */
  def errorUri: Option[URI]

  /** Tests whether `invalid_request` error. */
  def isInvalidRequest: Boolean

  /** Tests whether `invalid_token` error. */
  def isInvalidToken: Boolean

  /** Test whether `insufficient_scope` error. */
  def isInsufficientScope: Boolean
}

/** Factory for BearerChallenge. */
object BearerChallenge {
  /** Creates BearerChallenge with supplied credentials. */
  def apply(params: (String, String)*): BearerChallenge =
    BearerChallengeImpl(Params(params))

  /** Destructures BearerChallenge. */
  def unapply(auth: BearerChallenge): Option[(Option[String], Seq[String], Option[String], Map[String, String])] =
    Some(auth.realm, auth.scope, auth.error, auth.params)
}

private case class BearerChallengeImpl(params: Map[String, String]) extends BearerChallenge {
  lazy val realm: Option[String] = params.get("realm")
  lazy val scope: Seq[String] = params.get("scope")
    .map(_ split " ")
    .map(_.map(_.trim).toSeq)
    .map(_.filterNot(_.isEmpty))
    .getOrElse(Nil)

  lazy val error: Option[String] = params.get("error")
  lazy val errorDescription: Option[String] = params.get("error_description")
  lazy val errorUri: Option[URI] = params.get("error_uri").map(new URI(_))
  lazy val isInvalidRequest: Boolean = error.contains("invalid_request")
  lazy val isInvalidToken: Boolean = error.contains("invalid_token")
  lazy val isInsufficientScope: Boolean = error.contains("insufficient_scope")
}

/** Challenge factory */
object Challenge {
  /** Parses formatted challenge. */
  def parse(challenge: String): Challenge =
    ParseAuthType(challenge) match {
      case (scheme, token, params) => apply(scheme, token, params)
    }

  /** Parses formatted list of challenges. */
  def parseAll(challenges: String): Seq[Challenge] =
    SplitAuthTypes(challenges).map(parse)

  /** Creates Challenge with supplied scheme and parameters. */
  def apply(scheme: String, params: (String, String)*): Challenge =
    apply(scheme, None, Params(params))

  private def apply(scheme: String, token: Option[String], params: Map[String, String]): Challenge =
    scheme.toLowerCase match {
      case "basic" =>
        require(token.isEmpty, s"Invalid basic challenge: token not allowed")
        params.get("realm")
          .map(BasicChallenge(_, params.toSeq : _*))
          .getOrElse(throw new IllegalArgumentException("Invalid basic challenge: missing realm"))
      case "bearer" =>
        require(token.isEmpty, s"Invalid bearer challenge: token not allowed")
        BearerChallenge(params.toSeq : _*)
      case _ =>
        require(token.isEmpty, s"Invalid challenge: token not allowed")
        DefaultChallenge(scheme, params)
    }

  /** Destructures Challenge. */
  def unapply(challenge: Challenge): Option[(String, Map[String, String])] =
    Some((challenge.scheme, challenge.params))
}

private case class DefaultChallenge(scheme: String, params: Map[String, String]) extends Challenge

/**
 * Standardized type for Authorization and Proxy-Authorization header value.
 *
 * @see [[Authorization]]
 * @see [[ProxyAuthorization]]
 * @see [[Challenge]]
 */
trait Credentials extends AuthType {
  /** Gets token. */
  def token: String

  /** Returns formatted credentials. */
  override lazy val toString: String =
    scheme + " " + token
}

/** Credentials for Basic authorization. */
trait BasicCredentials extends Credentials {
  val scheme: String = "Basic"

  /** Gets user. */
  def user: String

  /** Gets password. */
  def password: String
}

/** Factory for BasicCredentials. */
object BasicCredentials {
  /** Creates BasicCredentials with supplied credentials. */
  def apply(token: String): BasicCredentials = {
    val valid = "(.+):(.*)".r

    Try(Base64.decodeToString(token))
      .collect { case valid(_, _) => BasicCredentialsImpl(token) }
      .getOrElse { throw new IllegalArgumentException(s"Invalid token: $token") }
  }

  /** Creates BasicCredentials with supplied credentials. */
  def apply(user: String, password: String): BasicCredentials =
    BasicCredentialsImpl(Base64.encodeToString(user + ":" + password))

  /** Destructures BasicCredentials. */
  def unapply(auth: BasicCredentials): Option[(String, String)] =
    Some(auth.user -> auth.password)
}

private case class BasicCredentialsImpl(token: String) extends BasicCredentials {
  def user: String = detoken(0)
  def password: String = detoken(1)

  private def detoken(part: Int): String =
    Base64.decodeToString(token).split(":", 2)(part)
}

/** Credentials for Bearer authorization. */
trait BearerCredentials extends Credentials {
  val scheme: String = "Bearer"
}

/** Factory for BearerCredentials. */
object BearerCredentials {
  /** Creates BearerCredentials with supplied credentials. */
  def apply(token: String): BearerCredentials =
    BearerCredentialsImpl(Token(token))

  /** Destructures BearerCredentials. */
  def unapply(auth: BearerCredentials): Option[String] =
    Some(auth.token)
}

private case class BearerCredentialsImpl(token: String) extends BearerCredentials

/** Credentials factory */
object Credentials {
  /** Parses formatted credentials. */
  def parse(credentials: String): Credentials =
    ParseAuthType(credentials) match {
      case (scheme, token, params) => apply(scheme, token, params)
    }

  /** Creates Credentials with supplied scheme and token. */
  def apply(scheme: String, token: String): Credentials =
    apply(scheme, Some(Token(token)), Map.empty[String, String])

  /** Creates Credentials with supplied scheme and parameters. */
  def apply(scheme: String, params: (String, String)*): Credentials =
    apply(scheme, None, Params(params))

  private def apply(scheme: String, token: Option[String], params: Map[String, String]): Credentials =
    scheme.toLowerCase match {
      case "basic" =>
        require(params.isEmpty, "Invalid basic credentials: params not allowed")
        token.map(BasicCredentials.apply)
          .getOrElse(throw new IllegalArgumentException("Token required for basic credentials"))
      case "bearer" =>
        require(params.isEmpty, "Invalid bearer credentials: params not allowed")
        token.map(BearerCredentials.apply)
          .getOrElse(throw new IllegalArgumentException("Token required for bearer credentials"))
      case _ =>
        require(params.isEmpty, "Invalid credentials: params not allowed")
        token.map(DefaultCredentials(scheme, _))
          .getOrElse(throw new IllegalArgumentException("Token required for credentials"))
    }

  /** Destructures Credentials. */
  def unapply(credentials: Credentials): Option[(String, String)] =
    Some((credentials.scheme, credentials.token))
}

private case class DefaultCredentials(scheme: String, token: String) extends Credentials
