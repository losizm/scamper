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

import scala.util.Try

import scamper.Base64
import AuthTypeHelper._

/** Base type for authentication header types. */
trait AuthType {
  /** Gets auth scheme. */
  def scheme: String

  /** Gets auth token. */
  def token: Option[String]

  /** Gets auth parameters. */
  def params: Map[String, String]

  /** Returns formatted auth type. */
  override lazy val toString: String =
    scheme + token.map(" " + _).getOrElse(AuthParams.format(params))
}

/**
 * Standardized type for WWW-Authenticate and Proxy-Authenticate header value.
 *
 * @see [[WwwAuthenticate]]
 * @see [[ProxyAuthenticate]]
 * @see [[Credentials]]
 */
trait Challenge extends AuthType

/** Challenge for Basic authentication. */
trait BasicAuthentication extends Challenge {
  val scheme: String = "Basic"

  /** Gets realm. */
  def realm: String
}

/** Factory for BasicAuthentication. */
object BasicAuthentication {
  /** Creates BasicAuthentication with supplied credentials. */
  def apply(realm: String, params: (String, String)*): BasicAuthentication =
    BasicAuthenticationImpl(realm, (params :+ ("realm" -> realm)).toMap)

  /** Destructures BasicAuthentication. */
  def unapply(auth: BasicAuthentication): Option[(String, Map[String, String])] =
    Some(auth.realm -> auth.params)
}

private case class BasicAuthenticationImpl(realm: String, params: Map[String, String]) extends BasicAuthentication {
  val token: Option[String] = None
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

  /** Creates Challenge with supplied auth scheme and token. */
  def apply(scheme: String, token: String): Challenge =
    apply(scheme, Some(Token(token)), Map.empty)

  /** Creates Challenge with supplied auth scheme and parameters. */
  def apply(scheme: String, params: Map[String, String]): Challenge =
    apply(scheme, None, params)

  private def apply(scheme: String, token: Option[String], params: Map[String, String]): Challenge =
    if (scheme.equalsIgnoreCase("basic"))
      params.get("realm")
        .map(BasicAuthentication(_, params.toSeq : _*))
        .getOrElse(throw new IllegalArgumentException("Invalid Basic authentication: missing realm"))
    else
      DefaultChallenge(scheme, token, params)

  /** Destructures Challenge. */
  def unapply(challenge: Challenge): Option[(String, Option[String], Map[String, String])] =
    Some((challenge.scheme, challenge.token, challenge.params))
}

private case class DefaultChallenge(scheme: String, token: Option[String], params: Map[String, String]) extends Challenge

/**
 * Standardized type for Authorization and Proxy-Authorization header value.
 *
 * @see [[Authorization]]
 * @see [[ProxyAuthorization]]
 * @see [[Challenge]]
 */
trait Credentials extends AuthType

/** Credentials for Basic authorization. */
trait BasicAuthorization extends Credentials {
  val scheme: String = "Basic"

  /** Gets user. */
  def user: String

  /** Gets password. */
  def password: String
}

/** Factory for BasicAuthorization. */
object BasicAuthorization {
  /** Creates BasicAuthorization with supplied credentials. */
  def apply(token: String): BasicAuthorization = {
    val valid = "(.+):(.*)".r

    Try(Base64.decodeToString(token))
      .collect { case valid(_, _) => BasicAuthorizationImpl(Some(token)) }
      .getOrElse { throw new IllegalArgumentException(s"Invalid token: $token") }
  }

  /** Creates BasicAuthorization with supplied credentials. */
  def apply(user: String, password: String): BasicAuthorization =
    BasicAuthorizationImpl(Some(Base64.encodeToString(user + ":" + password)))

  /** Destructures BasicAuthorization. */
  def unapply(auth: BasicAuthorization): Option[(String, String)] =
    Some(auth.user -> auth.password)
}

private case class BasicAuthorizationImpl(token: Option[String]) extends BasicAuthorization {
  val params: Map[String, String] = Map.empty

  def user: String = detoken(0)
  def password: String = detoken(1)

  private def detoken(part: Int): String =
    token.map(Base64.decodeToString)
      .map(_ split ":")
      .map(_(part))
      .get
}

/** Credentials factory */
object Credentials {
  /** Parses formatted credentials. */
  def parse(credentials: String): Credentials =
    ParseAuthType(credentials) match {
      case (scheme, token, params) => apply(scheme, token, params)
    }

  /** Creates Credentials with supplied auth scheme and token. */
  def apply(scheme: String, token: String): Credentials =
    apply(scheme, Some(Token(token)), Map.empty)

  /** Creates Credentials with supplied auth scheme and parameters. */
  def apply(scheme: String, params: Map[String, String]): Credentials =
    apply(scheme, None, params)

  private def apply(scheme: String, token: Option[String], params: Map[String, String]): Credentials =
    if (scheme.equalsIgnoreCase("basic"))
      token.map(BasicAuthorization.apply)
        .getOrElse(throw new IllegalArgumentException("Token required for Basic authorization"))
    else
      DefaultCredentials(scheme, token, params)

  /** Destructures Credentials. */
  def unapply(credentials: Credentials): Option[(String, Option[String], Map[String, String])] =
    Some((credentials.scheme, credentials.token, credentials.params))
}

private case class DefaultCredentials(scheme: String, token: Option[String], params: Map[String, String]) extends Credentials
