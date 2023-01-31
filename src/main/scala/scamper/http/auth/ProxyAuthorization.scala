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

/** Provides standardized access to Proxy-Authorization header. */
implicit class ProxyAuthorization(request: HttpRequest) extends AnyVal:
  /** Tests for Proxy-Authorization header. */
  def hasProxyAuthorization: Boolean =
    request.hasHeader("Proxy-Authorization")

  /**
   * Gets Proxy-Authorization header value.
   *
   * @throws HeaderNotFound if Proxy-Authorization is not present
   */
  def proxyAuthorization: Credentials =
    proxyAuthorizationOption.getOrElse(throw HeaderNotFound("Proxy-Authorization"))

  /** Gets Proxy-Authorization header value if present. */
  def proxyAuthorizationOption: Option[Credentials] =
    request.getHeaderValue("Proxy-Authorization").map(Credentials.parse)

  /**
   * Creates new request with Proxy-Authorization header set to supplied value.
   */
  def setProxyAuthorization(value: Credentials): HttpRequest =
    request.putHeaders(Header("Proxy-Authorization", value.toString))

  /** Creates new request with Proxy-Authorization header removed. */
  def proxyAuthorizationRemoved: HttpRequest =
    request.removeHeaders("Proxy-Authorization")

  /** Tests for basic proxy authorization. */
  def hasProxyBasic: Boolean =
    proxyBasicOption.isDefined

  /**
   * Gets basic proxy authorization.
   *
   * @throws HttpException if basic proxy authorization is not present
   */
  def proxyBasic: BasicCredentials =
    proxyBasicOption.getOrElse(throw HttpException("Basic proxy authorization not found"))

  /** Gets basic proxy authorization if present. */
  def proxyBasicOption: Option[BasicCredentials] =
    proxyAuthorizationOption.collect { case credentials: BasicCredentials => credentials }

  /** Creates new request with basic proxy authorization. */
  def setProxyBasic(token: String): HttpRequest =
    setProxyBasic(BasicCredentials(token))

  /** Creates new request with basic proxy authorization. */
  def setProxyBasic(user: String, password: String): HttpRequest =
    setProxyBasic(BasicCredentials(user, password))

  /** Creates new request with basic proxy authorization. */
  def setProxyBasic(credentials: BasicCredentials): HttpRequest =
    setProxyAuthorization(credentials)

  /** Tests for bearer proxy authorization. */
  def hasProxyBearer: Boolean =
    proxyBearerOption.isDefined

  /**
   * Gets bearer proxy authorization.
   *
   * @throws HttpException if bearer proxy authorization is not present
   */
  def proxyBearer: BearerCredentials =
    proxyBearerOption.getOrElse(throw HttpException("Bearer proxy authorization not found"))

  /** Gets bearer proxy authorization if present. */
  def proxyBearerOption: Option[BearerCredentials] =
    proxyAuthorizationOption.collect { case credentials: BearerCredentials => credentials }

  /** Creates new request with bearer proxy authorization. */
  def setProxyBearer(token: String): HttpRequest =
    setProxyBearer(BearerCredentials(token))

  /** Creates new request with bearer proxy authorization. */
  def setProxyBearer(credentials: BearerCredentials): HttpRequest =
    setProxyAuthorization(credentials)
