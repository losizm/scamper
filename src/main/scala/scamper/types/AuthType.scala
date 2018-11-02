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

import scamper.AuthParams
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
 * @see [[scamper.ImplicitHeaders.WWWAuthenticate]]
 * @see [[scamper.ImplicitHeaders.ProxyAuthenticate]]
 * @see [[Credentials]]
 */
trait Challenge extends AuthType

/** Challenge factory */
object Challenge {
  /** Parses formatted challenge. */
  def parse(challenge: String): Challenge =
    ParseAuthType(challenge) match {
      case (scheme, token, params) => ChallengeImpl(scheme, token, params)
    }

  /** Parses formatted list of challenges. */
  def parseAll(challenges: String): Seq[Challenge] =
    SplitAuthTypes(challenges).map(parse)

  /** Creates Challenge with supplied auth scheme and token. */
  def apply(scheme: String, token: String): Challenge =
    ChallengeImpl(scheme, Some(token), Map.empty)

  /** Creates Challenge with supplied auth scheme and parameters. */
  def apply(scheme: String, params: Map[String, String]): Challenge =
    ChallengeImpl(scheme, None, params)

  /** Creates Challenge with supplied auth scheme and parameters. */
  def apply(scheme: String, token: Option[String], params: Map[String, String]): Challenge =
    ChallengeImpl(scheme, token, params)

  /** Destructures Challenge. */
  def unapply(challenge: Challenge): Option[(String, Option[String], Map[String, String])] =
    Some((challenge.scheme, challenge.token, challenge.params))
}

private case class ChallengeImpl(scheme: String, token: Option[String], params: Map[String, String]) extends Challenge

/**
 * Standardized type for Authorization and Proxy-Authorization header value.
 *
 * @see [[scamper.ImplicitHeaders.Authorization]]
 * @see [[scamper.ImplicitHeaders.ProxyAuthorization]]
 * @see [[Challenge]]
 */
trait Credentials extends AuthType

/** Credentials factory */
object Credentials {
  /** Parses formatted credentials. */
  def parse(credentials: String): Credentials =
    ParseAuthType(credentials) match {
      case (scheme, token, params) => CredentialsImpl(scheme, token, params)
    }

  /** Creates Credentials with supplied auth scheme and token. */
  def apply(scheme: String, token: String): Credentials =
    CredentialsImpl(scheme, Some(token), Map.empty)

  /** Creates Credentials with supplied auth scheme and parameters. */
  def apply(scheme: String, params: Map[String, String]): Credentials =
    CredentialsImpl(scheme, None, params)

  /** Creates Credentials with supplied auth scheme and parameters. */
  def apply(scheme: String, token: Option[String], params: Map[String, String]): Credentials =
    CredentialsImpl(scheme, token, params)

  /** Destructures Credentials. */
  def unapply(credentials: Credentials): Option[(String, Option[String], Map[String, String])] =
    Some((credentials.scheme, credentials.token, credentials.params))
}

private case class CredentialsImpl(scheme: String, token: Option[String], params: Map[String, String]) extends Credentials