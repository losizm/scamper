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

/** Provides standardized access to Authorization header. */
implicit class Authorization(request: HttpRequest) extends AnyVal:
  /** Tests for Authorization header. */
  def hasAuthorization: Boolean =
    request.hasHeader("Authorization")

  /**
   * Gets Authorization header value.
   *
   * @throws HeaderNotFound if Authorization is not present
   */
  def authorization: Credentials =
    authorizationOption.getOrElse(throw HeaderNotFound("Authorization"))

  /** Gets Authorization header value if present. */
  def authorizationOption: Option[Credentials] =
    request.getHeaderValue("Authorization").map(Credentials.parse)

  /** Creates new request with Authorization header set to supplied value. */
  def setAuthorization(value: Credentials): HttpRequest =
    request.putHeaders(Header("Authorization", value.toString))

  /** Creates new request with Authorization header removed. */
  def authorizationRemoved: HttpRequest =
    request.removeHeaders("Authorization")

  /** Tests for basic authorization. */
  def hasBasic: Boolean =
    basicOption.isDefined

  /**
   * Gets basic authorization.
   *
   * @throws HttpException if basic authorization is not present
   */
  def basic: BasicCredentials =
    basicOption.getOrElse(throw HttpException("Basic authorization not found"))

  /** Gets basic authorization if present. */
  def basicOption: Option[BasicCredentials] =
    authorizationOption.collect { case credentials: BasicCredentials => credentials }

  /** Creates new request with basic authorization. */
  def setBasic(token: String): HttpRequest =
    setBasic(BasicCredentials(token))

  /** Creates new request with basic authorization. */
  def setBasic(user: String, password: String): HttpRequest =
    setBasic(BasicCredentials(user, password))

  /** Creates new request with basic authorization. */
  def setBasic(credentials: BasicCredentials): HttpRequest =
    setAuthorization(credentials)

  /** Tests for bearer authorization. */
  def hasBearer: Boolean =
    bearerOption.isDefined

  /**
   * Gets bearer authorization.
   *
   * @throws HttpException if bearer authorization is not present
   */
  def bearer: BearerCredentials =
    bearerOption.getOrElse(throw HttpException("Bearer authorization not found"))

  /** Gets bearer authorization if present. */
  def bearerOption: Option[BearerCredentials] =
    authorizationOption.collect { case credentials: BearerCredentials => credentials }

  /** Creates new request with bearer authorization. */
  def setBearer(token: String): HttpRequest =
    setBearer(BearerCredentials(token))

  /** Creates new request with bearer authorization. */
  def setBearer(credentials: BearerCredentials): HttpRequest =
    setAuthorization(credentials)
