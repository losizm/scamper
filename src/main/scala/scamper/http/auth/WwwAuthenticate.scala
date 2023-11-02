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

/** Adds standardized access to WWW-Authenticate header. */
given toWwwAuthenticate: Conversion[HttpResponse, WwwAuthenticate] = WwwAuthenticate(_)

/** Provides standardized access to WWW-Authenticate header. */
class WwwAuthenticate(response: HttpResponse) extends AnyVal:
  /** Tests for WWW-Authenticate header. */
  def hasWwwAuthenticate: Boolean =
    response.hasHeader("WWW-Authenticate")

  /**
   * Gets WWW-Authenticate header values.
   *
   * @return header values or empty sequence if WWW-Authenticate is not present
   */
  def wwwAuthenticate: Seq[Challenge] =
    wwwAuthenticateOption.getOrElse(Nil)

  /** Gets WWW-Authenticate header values if present. */
  def wwwAuthenticateOption: Option[Seq[Challenge]] =
    response.getHeaderValues("WWW-Authenticate")
      .flatMap(Challenge.parseAll) match
        case Nil => None
        case seq => Some(seq)

  /** Creates new response with WWW-Authenticate header set to supplied values. */
  def setWwwAuthenticate(values: Seq[Challenge]): HttpResponse =
    response.putHeaders(Header("WWW-Authenticate", values.mkString(", ")))

  /** Creates new response with WWW-Authenticate header set to supplied values. */
  def setWwwAuthenticate(one: Challenge, more: Challenge*): HttpResponse =
    setWwwAuthenticate(one +: more)

  /** Creates new response with WWW-Authenticate header removed. */
  def wwwAuthenticateRemoved: HttpResponse =
    response.removeHeaders("WWW-Authenticate")

  /** Tests for basic authentication. */
  def hasBasic: Boolean =
    basicOption.isDefined

  /**
   * Gets basic authentication.
   *
   * @throws HttpException if basic authentication is not present
   */
  def basic: BasicChallenge =
    basicOption.getOrElse(throw HttpException("Basic authentication not found"))

  /** Gets basic authentication if present. */
  def basicOption: Option[BasicChallenge] =
    wwwAuthenticate.collectFirst { case challenge: BasicChallenge => challenge }

  /** Creates new response with basic authentication. */
  def setBasic(realm: String, params: Map[String, String]): HttpResponse =
    setBasic(BasicChallenge(realm, params))

  /** Creates new response with basic authentication. */
  def setBasic(realm: String, params: (String, String)*): HttpResponse =
    setBasic(BasicChallenge(realm, params*))

  /** Creates new response with basic authentication. */
  def setBasic(challenge: BasicChallenge): HttpResponse =
    setWwwAuthenticate(challenge)

  /** Tests for bearer authentication. */
  def hasBearer: Boolean =
    bearerOption.isDefined

  /**
   * Gets bearer authentication.
   *
   * @throws HttpException if bearer authentication is not present
   */
  def bearer: BearerChallenge =
    bearerOption.getOrElse(throw HttpException("Bearer authentication not found"))

  /** Gets bearer authentication if present. */
  def bearerOption: Option[BearerChallenge] =
    wwwAuthenticate.collectFirst { case challenge: BearerChallenge => challenge }

  /** Creates new response with bearer authentication. */
  def setBearer(params: Map[String, String]): HttpResponse =
    setBearer(BearerChallenge(params))

  /** Creates new response with bearer authentication. */
  def setBearer(params: (String, String)*): HttpResponse =
    setBearer(BearerChallenge(params*))

  /** Creates new response with bearer authentication. */
  def setBearer(challenge: BearerChallenge): HttpResponse =
    setWwwAuthenticate(challenge)
