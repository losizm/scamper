package scamper.types

import AuthTypeHelper._

/** Base type for authentication header types. */
trait AuthType {
  /** Auth scheme */
  def scheme: String

  /** Auth token */
  def token: Option[String]

  /** Auth parameters */
  def params: Map[String, String]

  /** Returns formatted auth type. */
  override lazy val toString: String =
    scheme + token.map(" " + _).getOrElse(AuthParams.format(params))
}

/**
 * Standardized type for WWW-Authenticate and Proxy-Authenticate header value.
 *
 * @see [[scamper.headers.WWWAuthenticate]]
 * @see [[scamper.headers.ProxyAuthenticate]]
 * @see [[Credentials]]
 */
trait Challenge extends AuthType

/** Challenge factory */
object Challenge {
  /** Parses formatted challenge. */
  def apply(challenge: String): Challenge =
    ParseAuthType(challenge) match {
      case (scheme, token, params) => new ChallengeImpl(scheme, token, params)
    }

  /** Creates Challenge with supplied auth scheme and token. */
  def apply(scheme: String, token: String): Challenge =
    new ChallengeImpl(scheme, Some(token), Map.empty)

  /** Creates Challenge with supplied auth scheme and parameters. */
  def apply(scheme: String, params: Map[String, String]): Challenge =
    new ChallengeImpl(scheme, None, params)

  /** Creates Challenge with supplied auth scheme and parameters. */
  def apply(scheme: String, token: Option[String], params: Map[String, String]): Challenge =
    new ChallengeImpl(scheme, token, params)

  /** Destructures Challenge. */
  def unapply(challenge: Challenge): Option[(String, Option[String], Map[String, String])] =
    Some((challenge.scheme, challenge.token, challenge.params))
}

private class ChallengeImpl(val scheme: String, val token: Option[String], val params: Map[String, String]) extends Challenge

/**
 * Standardized type for Authorization and Proxy-Authorization header value.
 *
 * @see [[scamper.headers.Authorization]]
 * @see [[scamper.headers.ProxyAuthorization]]
 * @see [[Challenge]]
 */
trait Credentials extends AuthType

/** Credentials factory */
object Credentials {
  /** Parses formatted credentials. */
  def apply(credentials: String): Credentials =
    ParseAuthType(credentials) match {
      case (scheme, token, params) => new CredentialsImpl(scheme, token, params)
    }

  /** Creates Credentials with supplied auth scheme and token. */
  def apply(scheme: String, token: String): Credentials =
    new CredentialsImpl(scheme, Some(token), Map.empty)

  /** Creates Credentials with supplied auth scheme and parameters. */
  def apply(scheme: String, params: Map[String, String]): Credentials =
    new CredentialsImpl(scheme, None, params)

  /** Creates Credentials with supplied auth scheme and parameters. */
  def apply(scheme: String, token: Option[String], params: Map[String, String]): Credentials =
    new CredentialsImpl(scheme, token, params)

  /** Destructures Credentials. */
  def unapply(credentials: Credentials): Option[(String, Option[String], Map[String, String])] =
    Some((credentials.scheme, credentials.token, credentials.params))
}

private class CredentialsImpl(val scheme: String, val token: Option[String], val params: Map[String, String]) extends Credentials

