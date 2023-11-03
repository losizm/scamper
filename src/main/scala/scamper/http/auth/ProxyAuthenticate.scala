/*
 * Copyright 2021 Carlos Conyers
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
package scamper
package http
package auth

/** Adds standardized access to Proxy-Authenticate header. */
given toProxyAuthenticate: Conversion[HttpResponse, ProxyAuthenticate] = ProxyAuthenticate(_)

/** Adds standardized access to Proxy-Authenticate header. */
class ProxyAuthenticate(response: HttpResponse) extends AnyVal:
  /** Tests for Proxy-Authenticate. */
  def hasProxyAuthenticate: Boolean =
    response.hasHeader("Proxy-Authenticate")

  /**
   * Gets Proxy-Authenticate header values.
   *
   * @return header values or empty sequence if Proxy-Authenticate is not present
   */
  def proxyAuthenticate: Seq[Challenge] =
    proxyAuthenticateOption.getOrElse(Nil)

  /** Gets Proxy-Authenticate header values if present. */
  def proxyAuthenticateOption: Option[Seq[Challenge]] =
    response.getHeaderValues("Proxy-Authenticate")
      .flatMap(Challenge.parseAll) match
        case Nil => None
        case seq => Some(seq)

  /** Creates new response with Proxy-Authenticate header set to supplied values. */
  def setProxyAuthenticate(values: Seq[Challenge]): HttpResponse =
    response.putHeaders(Header("Proxy-Authenticate", values.mkString(", ")))

  /** Creates new response with Proxy-Authenticate header set to supplied values. */
  def setProxyAuthenticate(one: Challenge, more: Challenge*): HttpResponse =
    setProxyAuthenticate(one +: more)

  /** Creates new response with Proxy-Authenticate header removed. */
  def proxyAuthenticateRemoved: HttpResponse =
    response.removeHeaders("Proxy-Authenticate")

  /** Tests for basic proxy authentication. */
  def hasProxyBasic: Boolean =
    proxyBasicOption.isDefined

  /**
   * Gets basic proxy authentication.
   *
   * @throws HttpException if basic proxy authentication is not present
   */
  def proxyBasic: BasicChallenge =
    proxyBasicOption.getOrElse(throw HttpException("Basic proxy authentication not found"))

  /** Gets basic proxy authentication if present. */
  def proxyBasicOption: Option[BasicChallenge] =
    proxyAuthenticate.collectFirst { case challenge: BasicChallenge => challenge }

  /** Creates new response with basic proxy authentication. */
  def setProxyBasic(realm: String, params: Map[String, String]): HttpResponse =
    setProxyBasic(BasicChallenge(realm, params))

  /** Creates new response with basic proxy authentication. */
  def setProxyBasic(realm: String, params: (String, String)*): HttpResponse =
    setProxyBasic(BasicChallenge(realm, params*))

  /** Creates new response with basic proxy authentication. */
  def setProxyBasic(challenge: BasicChallenge): HttpResponse =
    setProxyAuthenticate(challenge)

  /** Tests for bearer proxy authentication. */
  def hasProxyBearer: Boolean =
    proxyBearerOption.isDefined

  /**
   * Gets bearer proxy authentication.
   *
   * @throws HttpException if bearer proxy authentication is not present
   */
  def proxyBearer: BearerChallenge =
    proxyBearerOption.getOrElse(throw HttpException("Bearer proxy authentication not found"))

  /** Gets bearer proxy authentication if present. */
  def proxyBearerOption: Option[BearerChallenge] =
    proxyAuthenticate.collectFirst { case challenge: BearerChallenge => challenge }

  /** Creates new response with bearer proxy authentication. */
  def setProxyBearer(params: Map[String, String]): HttpResponse =
    setProxyBearer(BearerChallenge(params))

  /** Creates new response with bearer proxy authentication. */
  def setProxyBearer(params: (String, String)*): HttpResponse =
    setProxyBearer(BearerChallenge(params*))

  /** Creates new response with bearer proxy authentication. */
  def setProxyBearer(challenge: BearerChallenge): HttpResponse =
    setProxyAuthenticate(challenge)
